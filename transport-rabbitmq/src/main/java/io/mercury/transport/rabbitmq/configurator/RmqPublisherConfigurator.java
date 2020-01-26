package io.mercury.transport.rabbitmq.configurator;

import javax.annotation.Nonnull;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.MessageProperties;

import io.mercury.common.util.Assertor;
import io.mercury.common.util.StringUtil;
import io.mercury.transport.rabbitmq.declare.ExchangeDeclare;
import io.mercury.transport.rabbitmq.declare.entity.Queue;

/**
 * 
 * @author yellow013
 * 
 */
public final class RmqPublisherConfigurator extends RmqConfigurator {

	// 发布者ExchangeDeclare
	private ExchangeDeclare exchangeDeclare;
	// 默认RoutingKey
	private String defaultRoutingKey;
	// 默认消息发布参数
	private BasicProperties msgProperties;
	// 是否进行发布确认
	private boolean confirm;
	// 发布确认超时时间
	private long confirmTimeout;
	// 发布确认重试次数
	private int confirmRetry;

	private RmqPublisherConfigurator(Builder builder) {
		super(builder.connection);
		this.exchangeDeclare = builder.exchangeDeclare;
		this.defaultRoutingKey = builder.defaultRoutingKey;
		this.msgProperties = builder.msgProperties;
		this.confirm = builder.confirm;
		this.confirmTimeout = builder.confirmTimeout;
		this.confirmRetry = builder.confirmRetry;
	}

	public static Builder configuration(@Nonnull RmqConnection connection) {
		return configuration(connection, ExchangeDeclare.Anonymous);
	}

	public static Builder configuration(@Nonnull RmqConnection connection, @Nonnull ExchangeDeclare exchangeDeclare) {
		return new Builder(Assertor.nonNull(connection, "connection"),
				Assertor.nonNull(exchangeDeclare, "exchangeDeclare"));
	}

	/**
	 * @return the exchangeDeclare
	 */
	public ExchangeDeclare exchangeDeclare() {
		return exchangeDeclare;
	}

	/**
	 * @return the defaultRoutingKey
	 */
	public String defaultRoutingKey() {
		return defaultRoutingKey;
	}

	/**
	 * @return the msgProperties
	 */
	public BasicProperties msgProperties() {
		return msgProperties;
	}

	/**
	 * @return the isConfirm
	 */
	public boolean confirm() {
		return confirm;
	}

	/**
	 * @return the confirmTimeout
	 */
	public long confirmTimeout() {
		return confirmTimeout;
	}

	/**
	 * @return the confirmRetry
	 */
	public int confirmRetry() {
		return confirmRetry;
	}

	private transient String toStringCache;

	@Override
	public String toString() {
		if (toStringCache == null)
			toStringCache = StringUtil.reflectionToString(this);
		return toStringCache;
	}

	public static class Builder {

		// 连接配置
		private RmqConnection connection;

		private ExchangeDeclare exchangeDeclare;

		private String defaultRoutingKey = "";
		private BasicProperties msgProperties = MessageProperties.PERSISTENT_BASIC;

		private boolean confirm = false;
		private long confirmTimeout = 5000;
		private int confirmRetry = 3;

		private Builder(RmqConnection connection) {
			this.connection = connection;
		}

		private Builder(RmqConnection connection, ExchangeDeclare exchangeDeclare) {
			this.connection = connection;
			this.exchangeDeclare = exchangeDeclare;
		}

		public RmqPublisherConfigurator build() {
			return new RmqPublisherConfigurator(this);
		}

		/**
		 * @param defaultRoutingKey the defaultRoutingKey to set
		 */
		public Builder defaultRoutingKey(String defaultRoutingKey) {
			this.defaultRoutingKey = defaultRoutingKey;
			return this;
		}

		/**
		 * @param msgProperties the msgProperties to set
		 */
		public Builder msgProperties(BasicProperties msgProperties) {
			this.msgProperties = msgProperties;
			return this;
		}

		/**
		 * @param isConfirm the isConfirm to set
		 */
		public Builder confirm(boolean confirm) {
			this.confirm = confirm;
			return this;
		}

		/**
		 * @param confirmTimeout the confirmTimeout to set
		 */
		public Builder confirmTimeout(long confirmTimeout) {
			this.confirmTimeout = confirmTimeout;
			return this;
		}

		/**
		 * @param confirmRetry the confirmRetry to set
		 */
		public Builder confirmRetry(int confirmRetry) {
			this.confirmRetry = confirmRetry;
			return this;
		}

	}

	public static void main(String[] args) {
		System.out.println(configuration(RmqConnection.configuration("localhost", 5672, "user0", "userpass").build(),
				ExchangeDeclare.direct("TEST").bindingQueue(Queue.named("TEST_0"))).build());
	}

}
