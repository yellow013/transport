package io.ffreedom.transport.rabbitmq;

import java.io.IOException;
import java.util.function.Consumer;

import javax.annotation.Nonnull;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

import io.ffreedom.common.charset.Charsets;
import io.ffreedom.common.log.ErrorLogger;
import io.ffreedom.transport.core.role.Receiver;
import io.ffreedom.transport.rabbitmq.config.ConnectionConfigurator;
import io.ffreedom.transport.rabbitmq.config.ReceiverConfigurator;
import io.ffreedom.transport.rabbitmq.declare.ExchangeDeclare;
import io.ffreedom.transport.rabbitmq.declare.QueueDeclare;

/**
 * 
 * @author yellow013
 * 
 *         改造升级, 使用共同的创建者建立Exchange, RoutingKey, Queue的绑定关系
 *
 */
public class RabbitMqReceiver extends BaseRabbitMqTransport implements Receiver {

	// 接收消息时使用的回调函数
	private volatile Consumer<byte[]> callback;

	// 接受者QueueDeclare
	private QueueDeclare queueDeclare;
	// 接受者Queue
	private String queueName;
	// 消息无法处理时发送到的错误消息ExchangeDeclare
	private ExchangeDeclare errorMsgExchange;
	// 消息无法处理时发送到的错误消息Exchange
	private String errorMsgExchangeName;
	// 是否有错误消息Exchange
	private boolean hasErrorMsgExchange;
	// 自动ACK
	private boolean autoAck;
	// 一次ACK多条
	private boolean multipleAck;
	// Ack最大自动重试次数
	private int maxAckTotal;
	// Ack最大自动重连次数
	private int maxAckReconnection;
	private int qos;

	private String receiverName;

	/**
	 * @param configurator
	 * @param callback
	 */
	public RabbitMqReceiver(String tag, @Nonnull ReceiverConfigurator configurator, Consumer<byte[]> callback) {
		super(tag, configurator.getConnectionConfigurator());
		this.callback = callback;
		this.queueDeclare = configurator.getQueueDeclare();
		this.errorMsgExchange = configurator.getErrorMsgExchange();
		this.autoAck = configurator.isAutoAck();
		this.multipleAck = configurator.isMultipleAck();
		this.maxAckTotal = configurator.getMaxAckTotal();
		this.maxAckReconnection = configurator.getMaxAckReconnection();
		this.qos = configurator.getQos();
		createConnection();
		init();
	}

	/**
	 * @param configurator
	 * @param callback
	 */
	@Deprecated
	public RabbitMqReceiver(String tag, ReceiverConfigurator configurator) {
		this(tag, configurator, null);
	}

	@Deprecated
	public boolean initCallback(Consumer<byte[]> callback) {
		if (this.callback != null)
			return false;
		this.callback = callback;
		return true;
	}

	private void init() {
		try {
			OperationalChannel operationalChannel = OperationalChannel.ofChannel(channel);
			this.queueDeclare.declare(operationalChannel);
			this.queueName = queueDeclare.getQueue().getName();
			if (errorMsgExchange != null) {
				this.errorMsgExchange.declare(operationalChannel);
				this.errorMsgExchangeName = errorMsgExchange.getExchange().getName();
				this.hasErrorMsgExchange = true;
			}
		} catch (Exception e) {
			// 在定义Queue和进行绑定时抛出任何异常都需要终止程序
			ErrorLogger.error(logger, e,
					"Call method declare() throw exception -> connection configurator info : {},"
							+ System.lineSeparator() + "error message : {}",
					connectionConfigurator.getConfiguratorName(), e.getMessage());
			destroy();
			throw new RuntimeException(e);
		}
		this.receiverName = "Receiver->" + connectionConfigurator.getConfiguratorName() + "$" + queueName;
	}

	@Override
	public void receive() {
		try {
			// channel.basicConsume(receiveQueue, isAutoAck, tag, (consumerTag, msg) -> {
			// Envelope envelope = msg.getEnvelope();
			// }, (consumerTag) -> {
			// }, (consumerTag, shutdownException) -> {
			// });
			if (!autoAck)
				channel.basicQos(qos);
			channel.basicConsume(
					// param1: queue
					queueName,
					// param2: autoAck
					autoAck,
					// param3: consumerTag
					tag,
					// param4: consumeCallback
					new DefaultConsumer(channel) {
						@Override
						public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties,
								byte[] body) throws IOException {
							try {
								logger.debug("Message handle start.");
								logger.debug(
										"Callback handleDelivery(consumerTag==[{}], envelope.getDeliveryTag==[{}] body.length==[{}])",
										consumerTag, envelope.getDeliveryTag(), body.length);
								callback.accept(body);
								logger.debug("Callback handleDelivery() end.");
							} catch (Exception e) {
								ErrorLogger.error(logger, e, "Call method callback.accept(body) throw Exception -> {}",
										e.getMessage());
								if (hasErrorMsgExchange) {
									// Sent message to error dump queue.
									logger.info("Exception handling -> Msg [{}] sent to ErrorMsgExchange.",
											new String(body, Charsets.UTF8));
									channel.basicPublish(errorMsgExchangeName, "", null, body);
									logger.info("Exception handling -> Sent to ErrorMsgExchange finished.");
								} else {
									// Reject message and close connection.
									logger.info("Exception handling -> Reject Msg [{}]",
											new String(body, Charsets.UTF8));
									channel.basicReject(envelope.getDeliveryTag(), true);
									logger.info("Exception handling -> Reject Msg finished.");
									destroy();
								}
							}
							if (!autoAck) {
								if (ack(envelope.getDeliveryTag()))
									logger.debug("Message handle end.");
								else {
									logger.info(
											"Call method ack(envelope.getDeliveryTag()==[{}]) failure. Reject message.");
									channel.basicReject(envelope.getDeliveryTag(), true);
								}
							}
						}
					});
		} catch (IOException e) {
			ErrorLogger.error(logger, e, "Call method channel.basicConsume() IOException message -> {}",
					e.getMessage());
		}
	}

	private boolean ack(long deliveryTag) {
		return ack0(deliveryTag, 0);
	}

	private boolean ack0(long deliveryTag, int retry) {
		if (retry == maxAckTotal) {
			logger.error("Has been retry ack {}, Quit ack.", maxAckTotal);
			return false;
		}
		logger.debug("Has been retry ack {}, Do next ack.", retry);
		try {
			int reconnectionCount = 0;
			while (!isConnected()) {
				logger.debug("Detect connection isConnected() == false, Reconnection count {}.", (++reconnectionCount));
				closeAndReconnection();
				if (reconnectionCount == maxAckReconnection) {
					logger.debug("Reconnection count -> {}, Quit current ack.", reconnectionCount);
					break;
				}
			}
			if (isConnected()) {
				logger.debug("Last detect connection isConnected() == true, Reconnection count {}", reconnectionCount);
				channel.basicAck(deliveryTag, multipleAck);
				logger.debug("Method channel.basicAck() finished.");
				return true;
			} else {
				logger.error("Last detect connection isConnected() == false, Reconnection count {}", reconnectionCount);
				logger.error("Unable to call method channel.basicAck()");
				return ack0(deliveryTag, retry);
			}
		} catch (IOException e) {
			ErrorLogger.error(logger, e,
					"Call method channel.basicAck(deliveryTag==[{}], multiple==[{}]) throw IOException -> {}",
					deliveryTag, multipleAck, e.getMessage());
			return ack0(deliveryTag, ++retry);
		}

	}

	@Override
	public boolean destroy() {
		logger.info("Call method RabbitMqReceiver.destroy()");
		closeConnection();
		return true;
	}

	@Override
	public String getName() {
		return receiverName;
	}

	public static void main(String[] args) {
		RabbitMqReceiver receiver = new RabbitMqReceiver("", ReceiverConfigurator
				.configuration(ConnectionConfigurator.configuration("", 5672, "", "").build(), QueueDeclare.queue(""))
				.build(), msg -> System.out.println(new String(msg, Charsets.UTF8)));
		receiver.receive();
	}

	@Override
	public void reconnect() {
		closeAndReconnection();
		receive();
	}

}
