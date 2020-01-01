package io.mercury.transport.rabbitmq;

import static io.mercury.common.util.StringUtil.isNullOrEmpty;
import static java.time.LocalDateTime.now;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import javax.annotation.Nonnull;

import org.slf4j.Logger;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Method;
import com.rabbitmq.client.ShutdownSignalException;

import io.mercury.common.functional.ShutdownEvent;
import io.mercury.common.log.CommonLoggerFactory;
import io.mercury.common.thread.ThreadUtil;
import io.mercury.common.util.Assertor;
import io.mercury.common.util.StringUtil;
import io.mercury.transport.core.TransportModule;
import io.mercury.transport.rabbitmq.configurator.RmqConnection;

abstract class AbstractRabbitMqTransport implements TransportModule {

	// 连接RabbitMQ Server使用的组件
	protected ConnectionFactory connectionFactory;
	protected volatile Connection connection;
	protected volatile Channel channel;

	// 存储配置信息对象
	protected RmqConnection rmqConnection;

	// 停机事件, 在监听到ShutdownSignalException时调用
	protected ShutdownEvent<Exception> shutdownEvent;

	// 子类共用Logger
	protected Logger logger = CommonLoggerFactory.getLogger(getClass());

	protected String tag;

	AbstractRabbitMqTransport() {
		// Generally not used
	}

	/**
	 * @param tag
	 * @param configurator
	 */
	AbstractRabbitMqTransport(String tag, @Nonnull String moduleType, @Nonnull RmqConnection rmqConnection) {
		this.tag = isNullOrEmpty(tag) ? moduleType + "-" + now() : tag;
		this.rmqConnection = Assertor.nonNull(rmqConnection, "rmqConnection");
		this.shutdownEvent = rmqConnection.shutdownEvent();
	}

	protected void createConnection() {
		logger.info("Call method createConnection()");
		if (connectionFactory == null) {
			connectionFactory = new ConnectionFactory();
			connectionFactory.setHost(rmqConnection.host());
			connectionFactory.setPort(rmqConnection.port());
			connectionFactory.setUsername(rmqConnection.username());
			connectionFactory.setPassword(rmqConnection.password());
			connectionFactory.setVirtualHost(rmqConnection.virtualHost());
			connectionFactory.setAutomaticRecoveryEnabled(rmqConnection.automaticRecovery());
			connectionFactory.setNetworkRecoveryInterval(rmqConnection.recoveryInterval());
			connectionFactory.setHandshakeTimeout(rmqConnection.handshakeTimeout());
			connectionFactory.setConnectionTimeout(rmqConnection.connectionTimeout());
			connectionFactory.setShutdownTimeout(rmqConnection.shutdownTimeout());
			connectionFactory.setRequestedHeartbeat(rmqConnection.requestedHeartbeat());
			if (rmqConnection.sslContext() != null)
				connectionFactory.useSslProtocol(rmqConnection.sslContext());
		}
		try {
			connection = connectionFactory.newConnection();
			logger.debug("Call method connectionFactory.newConnection() finished, tag -> {}, connection id -> {}.", tag,
					connection.getId());
			connection.addShutdownListener(shutdownSignalException -> {
				// 输出错误信息到控制台
				logger.info("Call lambda shutdown listener message -> {}", shutdownSignalException.getMessage());
				if (isNormalShutdown(shutdownSignalException))
					logger.info("{} -> normal shutdown.", tag);
				else {
					logger.info("{} -> is not normal shutdown.", tag);
					// 如果回调函数不为null, 则执行此函数
					if (shutdownEvent != null)
						shutdownEvent.accept(shutdownSignalException);
				}
			});
			channel = connection.createChannel();
			logger.debug("Call method connection.createChannel() finished, tag -> {}, channel number -> {}", tag,
					channel.getChannelNumber());
			logger.debug("All connection call method successful...");
		} catch (IOException e) {
			logger.error("Method createConnection() throw IOException -> {}", e.getMessage(), e);
		} catch (TimeoutException e) {
			logger.error("Method createConnection() throw TimeoutException -> {}", e.getMessage(), e);
		}
	}

	@Override
	public boolean isConnected() {
		return connection != null && connection.isOpen() && channel != null && channel.isOpen();
	}

	protected boolean closeAndReconnection() {
		logger.info("Call method closeAndReconnection()");
		closeConnection();
		ThreadUtil.sleep(rmqConnection.recoveryInterval() / 2);
		createConnection();
		ThreadUtil.sleep(rmqConnection.recoveryInterval() / 2);
		return isConnected();
	}

	private boolean isNormalShutdown(ShutdownSignalException sig) {
		Method reason = sig.getReason();
		if (reason instanceof AMQP.Channel.Close) {
			AMQP.Channel.Close channelClose = (AMQP.Channel.Close) reason;
			return channelClose.getReplyCode() == AMQP.REPLY_SUCCESS
					&& StringUtil.isEquals(channelClose.getReplyText(), "OK");
		} else if (reason instanceof AMQP.Connection.Close) {
			AMQP.Connection.Close connectionClose = (AMQP.Connection.Close) reason;
			return connectionClose.getReplyCode() == AMQP.REPLY_SUCCESS
					&& StringUtil.isEquals(connectionClose.getReplyText(), "OK");
		} else
			return false;
	}

	protected void closeConnection() {
		logger.info("Call method closeConnection()");
		try {
			if (channel != null && channel.isOpen()) {
				channel.close();
				logger.info("Channel is closeed!");
			}
			if (connection != null && connection.isOpen()) {
				connection.close();
				logger.info("Connection is closeed!");
			}
		} catch (IOException e) {
			logger.error("Method closeConnection() throw IOException -> {}", e.getMessage(), e);
		} catch (TimeoutException e) {
			logger.error("Method closeConnection() throw TimeoutException -> {}", e.getMessage(), e);
		}
	}

}
