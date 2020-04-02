package io.mercury.transport.rabbitmq;

import static io.mercury.common.util.StringUtil.bytesToStr;

import java.io.IOException;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.annotation.Nonnull;

import com.rabbitmq.client.AMQP.BasicProperties;

import io.mercury.common.character.Charsets;
import io.mercury.common.thread.ThreadUtil;
import io.mercury.common.util.Assertor;
import io.mercury.transport.core.api.Publisher;
import io.mercury.transport.core.exception.FailPublishException;
import io.mercury.transport.rabbitmq.configurator.RmqConnection;
import io.mercury.transport.rabbitmq.configurator.RmqPublisherConfigurator;
import io.mercury.transport.rabbitmq.declare.ExchangeAndBinding;
import io.mercury.transport.rabbitmq.exception.AmqpDeclareException;
import io.mercury.transport.rabbitmq.exception.AmqpDeclareRuntimeException;
import io.mercury.transport.rabbitmq.exception.AmqpNoConfirmException;

public class RabbitMqPublisher extends AbstractRabbitMqTransport implements Publisher<byte[]> {

	// 发布消息使用的[ExchangeDeclare]
	private final ExchangeAndBinding publishExchange;
	// 发布消息使用的[Exchange]
	private final String exchangeName;

	// 发布消息使用的默认[RoutingKey]
	private final String defaultRoutingKey;
	// 发布消息使用的默认[MessageProperties]
	private final BasicProperties defaultMsgProperties;
	//
	private final Supplier<BasicProperties> msgPropertiesSupplier;

	private final boolean confirm;
	private final long confirmTimeout;
	private final int confirmRetry;

	private final String publisherName;

	private boolean hasPropertiesSupplier;

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
		this.exchangeName = publishExchange.exchangeName();
		this.defaultRoutingKey = configurator.defaultRoutingKey();
		this.defaultMsgProperties = configurator.defaultMsgProperties();
		this.msgPropertiesSupplier = configurator.msgPropertiesSupplier();
		this.confirm = configurator.confirm();
		this.confirmTimeout = configurator.confirmTimeout();
		this.confirmRetry = configurator.confirmRetry();
		this.ackCallback = ackCallback;
		this.noAckCallback = noAckCallback;
		this.hasPropertiesSupplier = (msgPropertiesSupplier != null);
		this.publisherName = "publisher::" + rmqConnection.fullInfo() + "$" + exchangeName;
		createConnection();
		declare();
	}

	private void declare() throws AmqpDeclareRuntimeException {
		try {
			if (publishExchange == ExchangeAndBinding.Anonymous)
				log.warn(
						"Publisher-> {} use anonymous exchange, Please specify [queue name] as the [routing key] when publish",
						tag);
			else
				this.publishExchange.declare(RabbitMqDeclarant.withChannel(channel));
		} catch (AmqpDeclareException e) {
			// 在定义Exchange和进行绑定时抛出任何异常都需要终止程序
			log.error("Exchange declare throw exception -> connection configurator info : {}	, error message : {}",
					rmqConnection.fullInfo(), e.getMessage(), e);
			destroy();
			throw new AmqpDeclareRuntimeException(e);
		}

	}

	@Override
	public void publish(byte[] msg) throws FailPublishException {
		publish(defaultRoutingKey, msg, defaultMsgProperties);
	}

	@Override
	public void publish(String target, byte[] msg) throws FailPublishException {
		publish(target, msg, hasPropertiesSupplier ? msgPropertiesSupplier.get() : defaultMsgProperties);
	}

	public void publish(String target, byte[] msg, BasicProperties props) throws FailPublishException {
		// 记录重试次数
		int retry = 0;
		// 调用isConnected()检查channel和connection是否打开, 如果没有打开, 先销毁连接, 再重新创建连接.
		while (!isConnected()) {
			log.error("Detect connection isConnected() == false, retry {}", (++retry));
			destroy();
			ThreadUtil.sleep(rmqConnection.recoveryInterval());
			createConnection();
		}
		if (confirm) {
			try {
				confirmPublish(target, msg, props);
			} catch (IOException e) {
				log.error("Method publish isConfirm==[true] throw IOException -> {}, msg==[{}]", e.getMessage(),
						bytesToStr(msg), e);
				destroy();
				throw new FailPublishException(e);
			} catch (AmqpNoConfirmException e) {
				log.error("Method publish isConfirm==[true] throw NoConfirmException -> {}, msg==[{}]", e.getMessage(),
						bytesToStr(msg), e);
				throw new FailPublishException(e);
			}
		} else {
			try {
				basicPublish(target, msg, props);
			} catch (IOException e) {
				log.error("Method publish isConfirm==[false] throw IOException -> {}, msg==[{}]", e.getMessage(),
						bytesToStr(msg), e);
				destroy();
				throw new FailPublishException(e);
			}
		}
	}

	private void confirmPublish(String routingKey, byte[] msg, BasicProperties props)
			throws IOException, AmqpNoConfirmException {
		confirmPublish0(routingKey, msg, props, 0);
	}

	// TODO 优化异常处理逻辑
	private void confirmPublish0(String routingKey, byte[] msg, BasicProperties props, int retry)
			throws IOException, AmqpNoConfirmException {
		try {
			channel.confirmSelect();
			basicPublish(routingKey, msg, props);
			if (channel.waitForConfirms(confirmTimeout))
				return;
			log.error("Call method channel.waitForConfirms(confirmTimeout==[{}]) retry==[{}]", confirmTimeout, retry);
			if (++retry == confirmRetry)
				throw new AmqpNoConfirmException(exchangeName, routingKey, retry, confirmTimeout);
			confirmPublish0(routingKey, msg, props, retry);
		} catch (IOException e) {
			log.error("Method channel.confirmSelect() throw IOException from publisherName -> {}, routingKey -> {}",
					publisherName, routingKey, e);
			throw e;
		} catch (InterruptedException e) {
			log.error(
					"Method channel.waitForConfirms() throw InterruptedException from publisherName -> {}, routingKey -> {}",
					publisherName, routingKey, e);
		} catch (TimeoutException e) {
			log.error(
					"Method channel.waitForConfirms() throw TimeoutException from publisherName -> {}, routingKey -> {}",
					publisherName, routingKey, e);
		}
	}

	private void basicPublish(String routingKey, byte[] msg, BasicProperties props) throws IOException {
		try {
			channel.basicPublish(
					// param1: the exchange to publish the message to
					exchangeName,
					// param2: the routing key
					routingKey,
					// param3: other properties for the message - routing headers etc
					props,
					// param4: the message body
					msg);
		} catch (IOException e) {
			StringBuilder propsStringBuilder = new StringBuilder(220);
			props.appendPropertyDebugStringTo(propsStringBuilder);
			log.error(
					"Method channel.basicPublish(exchange==[{}], routingKey==[{}], properties==[{}], msg==[...]) throw IOException -> {}",
					exchangeName, routingKey, propsStringBuilder.toString(), e.getMessage(), e);
			throw e;
		}
	}

	@Override
	public boolean destroy() {
		log.info("Call method destroy() from Publisher name==[{}]", publisherName);
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
