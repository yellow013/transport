package io.ffreedom.transport.rabbitmq;

import java.io.IOException;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

import javax.annotation.Nonnull;
import javax.net.ssl.SSLContext;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.BuiltinExchangeType;

import io.ffreedom.common.charset.Charsets;
import io.ffreedom.common.log.ErrorLogger;
import io.ffreedom.common.thread.ThreadUtil;
import io.ffreedom.common.utils.StringUtil;
import io.ffreedom.transport.core.role.Publisher;
import io.ffreedom.transport.rabbitmq.RabbitMqOperatingTools.OperationalChannel;
import io.ffreedom.transport.rabbitmq.config.PublisherConfigurator;

public class RabbitMqPublisher extends BaseRabbitMqTransport implements Publisher<byte[]> {

	// 发布消息使用的routingKey
	private String routingKey;
	// 发布消息使用的exchange
	private String exchange;
	// 发布消息使用的参数
	private BasicProperties msgProperties;
	private String publisherName;
	private String[] bindQueues;
	private BuiltinExchangeType builtinExchangeType;

	private boolean isConfirm;
	private long confirmTimeout;
	private int confirmRetry;

	@SuppressWarnings("unused")
	private Consumer<Long> ackCallback;

	@SuppressWarnings("unused")
	private Consumer<Long> noAckCallback;

	private PublisherConfigurator configurator;

	/**
	 * @param configurator
	 */
	public RabbitMqPublisher(String tag, PublisherConfigurator configurator) {
		this(tag, configurator, null, null, null);
	}

	public RabbitMqPublisher(String tag, PublisherConfigurator configurator, SSLContext sslContext) {
		this(tag, configurator, sslContext, null, null);
	}

	public RabbitMqPublisher(String tag, @Nonnull PublisherConfigurator configurator, SSLContext sslContext,
			Consumer<Long> ackCallback, Consumer<Long> noAckCallback) {
		super(tag, configurator.getConnectionConfigurator());
		this.configurator = configurator;
		this.exchange = configurator.getExchange();
		this.routingKey = configurator.getRoutingKey();
		this.msgProperties = configurator.getMsgProperties();
		this.bindQueues = configurator.getBindQueues();
		this.builtinExchangeType = configurator.getBuiltinExchangeType();
		this.isConfirm = configurator.isConfirm();
		this.confirmTimeout = configurator.getConfirmTimeout();
		this.confirmRetry = configurator.getConfirmRetry();
		createConnection();
		init();
	}

	private void init() {
		this.publisherName = "Publisher->" + connectionConfigurator.getConfiguratorName() + "$" + routingKey;
		try {
			OperationalChannel operationalChannel = RabbitMqOperatingTools.ofChannel(channel);
			// 根据不同的Exchange类型, 创建Exchange
			switch (builtinExchangeType) {
			case DIRECT:
				operationalChannel.declareDirectExchange(exchange, configurator.isDurable(),
						configurator.isAutoDelete(), configurator.isInternal());
				break;
			case FANOUT:
				operationalChannel.declareFanoutExchange(exchange, configurator.isDurable(),
						configurator.isAutoDelete(), configurator.isInternal());
				break;
			case TOPIC:
				operationalChannel.declareTopicExchange(exchange, configurator.isDurable(), configurator.isAutoDelete(),
						configurator.isInternal());
				break;
			default:
				break;
			}
			if (bindQueues != null) {
				for (String queue : bindQueues) {
					if (!StringUtil.isNullOrEmpty(queue)) {
						operationalChannel.declareQueue(queue, configurator.isDurable(), configurator.isExclusive(),
								configurator.isAutoDelete());
						operationalChannel.bindQueue(queue, exchange, routingKey);
					}
				}
			}
		} catch (IOException e) {
			ErrorLogger.error(logger, e, "Call method init() throw IOException -> {}", e.getMessage());
			destroy();
		} finally {

		}
	}

	@Override
	public void publish(byte[] msg) {
		publish(routingKey, msg);
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
		} catch (NoAckException e) {
			ErrorLogger.error(logger, e, "Call method publish() isConfirm==[{}] throw AckRetryException -> {} ",
					isConfirm, e.getMessage());
		}
	}

	private void confirmPublish(String target, byte[] msg) throws IOException, NoAckException {
		confirmPublish0(target, msg, 0);
	}

	private void confirmPublish0(String target, byte[] msg, int retry) throws IOException, NoAckException {
		try {
			channel.confirmSelect();
			basicPublish(target, msg);
			if (channel.waitForConfirms(confirmTimeout))
				return;
			logger.error("Call method channel.waitForConfirms(confirmTimeout==[{}]) retry==[{}]", confirmTimeout,
					retry);
			if (++retry == confirmRetry)
				throw new NoAckException(target, retry);
			confirmPublish0(target, msg, retry);
		} catch (IOException e) {
			ErrorLogger.error(logger, e,
					"Call method channel.confirmSelect() throw IOException from publisherName -> {}, target -> {}",
					publisherName, target);
			throw new IOException(e.getMessage());
		} catch (InterruptedException e) {
			ErrorLogger.error(logger, e,
					"Call method channel.waitForConfirms() throw InterruptedException from publisherName -> {}, target -> {}",
					publisherName, target);
		} catch (TimeoutException e) {
			ErrorLogger.error(logger, e,
					"Call method channel.waitForConfirms() throw TimeoutException from publisherName -> {}, target -> {}",
					publisherName, target);
		}
	}

	private void basicPublish(String target, byte[] msg) throws IOException {
		try {
			channel.basicPublish(
					// param1: exchange
					exchange,
					// param2: routingKey
					target,
					// param3: properties
					msgProperties,
					// param4: msgBody
					msg);
		} catch (IOException e) {
			ErrorLogger.error(logger, e,
					"Call method channel.basicPublish(exchange==[{}], routingKey==[{}], properties==[{}], msg==[...]) throw IOException -> {}",
					exchange, target, msgProperties, e.getMessage());
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

	public class NoAckException extends Exception {

		/**
		 *
		 */
		private static final long serialVersionUID = -197190157920481972L;

		public NoAckException(String exchange, int retry) {
			super("Call confirmPublish(target==[" + exchange + "] msg==[...] retry==[" + retry + "]) failure.");
		}

	}

	public class ResendCounter {

	}

	public static void main(String[] args) {

		RabbitMqPublisher publisher = new RabbitMqPublisher("",
				PublisherConfigurator.configuration("", 5672, "", "").setFanoutExchange(""));

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
