package io.ffreedom.transport.rabbitmq;

import java.io.IOException;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

import javax.annotation.Nonnull;

import com.rabbitmq.client.AMQP.BasicProperties;

import io.ffreedom.common.charset.Charsets;
import io.ffreedom.common.log.ErrorLogger;
import io.ffreedom.common.thread.ThreadUtil;
import io.ffreedom.transport.core.api.Publisher;
import io.ffreedom.transport.rabbitmq.config.ConnectionConfigurator;
import io.ffreedom.transport.rabbitmq.config.RmqPublisherConfigurator;
import io.ffreedom.transport.rabbitmq.declare.ExchangeDeclare;
import io.ffreedom.transport.rabbitmq.exception.NoConfirmException;

public class RabbitMqPublisher extends BaseRabbitMqTransport implements Publisher<byte[]> {

	// 发布消息使用的ExchangeDeclare
	private ExchangeDeclare exchangeDeclare;
	// 发布消息使用的Exchange
	private String exchangeName;
	// 发布消息使用的默认RoutingKey
	private String defaultRoutingKey;
	private BasicProperties msgProperties;

	private boolean isConfirm;
	private long confirmTimeout;
	private int confirmRetry;

	private String publisherName;

	@SuppressWarnings("unused")
	private Consumer<Long> ackCallback;

	@SuppressWarnings("unused")
	private Consumer<Long> noAckCallback;

	// private PublisherConfigurator configurator;

	/**
	 * @param configurator
	 */
	public RabbitMqPublisher(String tag, @Nonnull RmqPublisherConfigurator configurator) {
		this(tag, configurator, null, null);
	}

	public RabbitMqPublisher(String tag, @Nonnull RmqPublisherConfigurator configurator, Consumer<Long> ackCallback,
			Consumer<Long> noAckCallback) {
		super(tag, configurator.getConnectionConfigurator());
		// this.configurator = configurator;
		this.exchangeDeclare = configurator.getExchangeDeclare();
		this.defaultRoutingKey = configurator.getDefaultRoutingKey();
		this.msgProperties = configurator.getMsgProperties();
		this.isConfirm = configurator.isConfirm();
		this.confirmTimeout = configurator.getConfirmTimeout();
		this.confirmRetry = configurator.getConfirmRetry();
		this.ackCallback = ackCallback;
		this.noAckCallback = noAckCallback;
		createConnection();
		init();
	}

	private void init() {
		try {
			this.exchangeDeclare.declare(OperationalChannel.ofChannel(channel));
			this.exchangeName = exchangeDeclare.getExchange().getName();
		} catch (Exception e) {
			// 在定义Exchange和进行绑定时抛出任何异常都需要终止程序
			ErrorLogger.error(logger, e,
					"Call method declare() throw exception -> connection configurator info : {}	, error message : {}",
					connectionConfigurator.getConfiguratorName(), e.getMessage());
			destroy();
			throw new RuntimeException(e);
		}
		this.publisherName = "Publisher->" + connectionConfigurator.getConfiguratorName() + "$" + exchangeName;
	}

	@Override
	public void publish(byte[] msg) {
		publish(defaultRoutingKey, msg);
	}

	@Override
	public void publish(String target, byte[] msg) {
		try {
			// 记录重试次数
			int retry = 0;
			// 调用isConnected()检查channel和connection是否打开, 如果没有打开, 先销毁连接, 再重新创建连接.
			while (!isConnected()) {
				logger.error("Detect connection isConnected() == false, retry {}", (++retry));
				destroy();
				ThreadUtil.sleep(connectionConfigurator.getRecoveryInterval());
				createConnection();
			}
			if (isConfirm)
				confirmPublish(target, msg);
			else
				basicPublish(target, msg);
		} catch (IOException e) {
			ErrorLogger.error(logger, e, "Call method publish() isConfirm==[{}] throw IOException -> {} ", isConfirm,
					e.getMessage());
			destroy();
		} catch (NoConfirmException e) {
			ErrorLogger.error(logger, e, "Call method publish() isConfirm==[{}] throw NoConfirmException -> {} ",
					isConfirm, e.getMessage());
		}
	}

	private void confirmPublish(String routingKey, byte[] msg) throws IOException, NoConfirmException {
		confirmPublish0(routingKey, msg, 0);
	}

	private void confirmPublish0(String routingKey, byte[] msg, int retry) throws IOException, NoConfirmException {
		try {
			channel.confirmSelect();
			basicPublish(routingKey, msg);
			if (channel.waitForConfirms(confirmTimeout))
				return;
			logger.error("Call method channel.waitForConfirms(confirmTimeout==[{}]) retry==[{}]", confirmTimeout,
					retry);
			if (++retry == confirmRetry)
				throw new NoConfirmException(exchangeName, routingKey, retry, confirmTimeout);
			confirmPublish0(routingKey, msg, retry);
		} catch (IOException e) {
			ErrorLogger.error(logger, e,
					"Call method channel.confirmSelect() throw IOException from publisherName -> {}, routingKey -> {}",
					publisherName, routingKey);
			throw new IOException(e.getMessage());
		} catch (InterruptedException e) {
			ErrorLogger.error(logger, e,
					"Call method channel.waitForConfirms() throw InterruptedException from publisherName -> {}, routingKey -> {}",
					publisherName, routingKey);
		} catch (TimeoutException e) {
			ErrorLogger.error(logger, e,
					"Call method channel.waitForConfirms() throw TimeoutException from publisherName -> {}, routingKey -> {}",
					publisherName, routingKey);
		}
	}

	private void basicPublish(String routingKey, byte[] msg) throws IOException {
		try {
			channel.basicPublish(
					// param1: exchange
					exchangeName,
					// param2: routingKey
					routingKey,
					// param3: properties
					msgProperties,
					// param4: msgBody
					msg);
		} catch (IOException e) {
			ErrorLogger.error(logger, e,
					"Call method channel.basicPublish(exchange==[{}], routingKey==[{}], properties==[{}], msg==[...]) throw IOException -> {}",
					exchangeName, routingKey, msgProperties, e.getMessage());
			throw new IOException(e.getMessage());
		}
	}

	@Override
	public boolean destroy() {
		logger.info("Call method -> RabbitPublisher.destroy()");
		closeConnection();
		return true;
	}

	@Override
	public String getName() {
		return publisherName;
	}

	public class ResendCounter {

	}

	public static void main(String[] args) {

		ConnectionConfigurator connectionConfigurator0 = ConnectionConfigurator.configuration("", 5672, "", "").build();

		ExchangeDeclare fanoutExchange = ExchangeDeclare.fanoutExchange("");

		RabbitMqPublisher publisher = new RabbitMqPublisher("",
				RmqPublisherConfigurator.configuration(connectionConfigurator0, fanoutExchange).build());

		ThreadUtil.startNewThread(() -> {
			int count = 0;
			while (true) {
				ThreadUtil.sleep(5000);
				publisher.publish(String.valueOf(++count).getBytes(Charsets.UTF8));
				System.out.println(count);
			}
		});

	}

}
