package io.mercury.transport.rabbitmq;

import static io.mercury.common.util.StringUtil.bytesToStr;

import java.io.IOException;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

import javax.annotation.Nonnull;

import com.rabbitmq.client.AMQP.BasicProperties;

import io.mercury.common.character.Charsets;
import io.mercury.common.thread.ThreadUtil;
import io.mercury.common.util.Assertor;
import io.mercury.transport.core.api.Publisher;
import io.mercury.transport.rabbitmq.configurator.RmqConnection;
import io.mercury.transport.rabbitmq.configurator.RmqPublisherConfigurator;
import io.mercury.transport.rabbitmq.declare.ExchangeAndBinding;
import io.mercury.transport.rabbitmq.exception.AmqpDeclareException;
import io.mercury.transport.rabbitmq.exception.AmqpDeclareRuntimeException;
import io.mercury.transport.rabbitmq.exception.NoConfirmException;

public class RabbitMqPublisher extends AbstractRabbitMqTransport implements Publisher<byte[]> {

	// 发布消息使用的ExchangeDeclare
	private ExchangeAndBinding publishExchange;
	// 发布消息使用的Exchange
	private String exchangeName;
	// 发布消息使用的默认RoutingKey
	private String defaultRoutingKey;
	private BasicProperties msgProperties;

	private boolean confirm;
	private long confirmTimeout;
	private int confirmRetry;

	private String publisherName;

	@SuppressWarnings("unused")
	private Consumer<Long> ackCallback;

	@SuppressWarnings("unused")
	private Consumer<Long> noAckCallback;

	/**
	 * 
	 * @param configurator
	 */
	public RabbitMqPublisher(@Nonnull RmqPublisherConfigurator configurator) {
		this(null, configurator, null, null);
	}

	/**
	 * 
	 * @param tag
	 * @param configurator
	 */
	public RabbitMqPublisher(String tag, @Nonnull RmqPublisherConfigurator configurator) {
		this(tag, configurator, null, null);
	}

	/**
	 * 
	 * @param tag
	 * @param configurator
	 * @param ackCallback
	 * @param noAckCallback
	 */
	public RabbitMqPublisher(String tag, @Nonnull RmqPublisherConfigurator configurator, Consumer<Long> ackCallback,
			Consumer<Long> noAckCallback) {
		super(tag, "publisher", configurator.connection());
		this.publishExchange = Assertor.nonNull(configurator.publishExchange(), "exchangeRelation");
		this.defaultRoutingKey = configurator.defaultRoutingKey();
		this.msgProperties = configurator.msgProperties();
		this.confirm = configurator.confirm();
		this.confirmTimeout = configurator.confirmTimeout();
		this.confirmRetry = configurator.confirmRetry();
		this.ackCallback = ackCallback;
		this.noAckCallback = noAckCallback;
		createConnection();
		declare();
		this.publisherName = "publisher::" + rmqConnection.fullInfo() + "$" + exchangeName;
	}

	private void declare() {
		try {
			if (publishExchange == ExchangeAndBinding.Anonymous)
				logger.warn(
						"Publisher-> {} use anonymous exchange, Please specify [queue name] as the [routing key] when publish",
						tag);
			else
				this.publishExchange.declare(RabbitMqDeclarant.withChannel(channel));
		} catch (AmqpDeclareException e) {
			// 在定义Exchange和进行绑定时抛出任何异常都需要终止程序
			logger.error("Exchange declare throw exception -> connection configurator info : {}	, error message : {}",
					rmqConnection.fullInfo(), e.getMessage(), e);
			destroy();
			throw new AmqpDeclareRuntimeException(e);
		}
		this.exchangeName = publishExchange.exchangeName();
	}

	@Override
	public void publish(byte[] msg) {
		publish(defaultRoutingKey, msg);
	}

	@Override
	public void publish(String target, byte[] msg) {
		// 记录重试次数
		int retry = 0;
		// 调用isConnected()检查channel和connection是否打开, 如果没有打开, 先销毁连接, 再重新创建连接.
		while (!isConnected()) {
			logger.error("Detect connection isConnected() == false, retry {}", (++retry));
			destroy();
			ThreadUtil.sleep(rmqConnection.recoveryInterval());
			createConnection();
		}
		if (confirm) {
			try {
				confirmPublish(target, msg);
			} catch (IOException e) {
				logger.error("Method publish isConfirm==[true] throw IOException -> {}, msg==[{}]", e.getMessage(),
						bytesToStr(msg), e);
				destroy();
			} catch (NoConfirmException e) {
				logger.error("Method publish isConfirm==[true] throw NoConfirmException -> {}, msg==[{}]",
						e.getMessage(), bytesToStr(msg), e);
			}
		} else {
			try {
				basicPublish(target, msg);
			} catch (IOException e) {
				logger.error("Method publish isConfirm==[false] throw IOException -> {}, msg==[{}]", e.getMessage(),
						bytesToStr(msg), e);
				destroy();
			}
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
			logger.error("Method channel.confirmSelect() throw IOException from publisherName -> {}, routingKey -> {}",
					publisherName, routingKey, e);
			throw new IOException(e.getMessage());
		} catch (InterruptedException e) {
			logger.error(
					"Method channel.waitForConfirms() throw InterruptedException from publisherName -> {}, routingKey -> {}",
					publisherName, routingKey, e);
		} catch (TimeoutException e) {
			logger.error(
					"Method channel.waitForConfirms() throw TimeoutException from publisherName -> {}, routingKey -> {}",
					publisherName, routingKey, e);
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
			logger.error(
					"Method channel.basicPublish(exchange==[{}], routingKey==[{}], properties==[{}], msg==[...]) throw IOException -> {}",
					exchangeName, routingKey, msgProperties, e.getMessage(), e);
			throw new IOException(e.getMessage());
		}
	}

	@Override
	public boolean destroy() {
		logger.info("Call method destroy() from Publisher name==[{}]", publisherName);
		return super.destroy();
	}

	@Override
	public String name() {
		return publisherName;
	}

	public class ResendCounter {

	}

	public static void main(String[] args) {

		RmqConnection connectionConfigurator0 = RmqConnection.configuration("", 5672, "", "").build();

		ExchangeAndBinding fanoutExchange = ExchangeAndBinding.fanout("");

		try (RabbitMqPublisher publisher = new RabbitMqPublisher("",
				RmqPublisherConfigurator.configuration(connectionConfigurator0, fanoutExchange).build())) {
			ThreadUtil.startNewThread(() -> {
				int count = 0;
				while (true) {
					ThreadUtil.sleep(5000);
					publisher.publish(String.valueOf(++count).getBytes(Charsets.UTF8));
					System.out.println(count);
				}
			});
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
