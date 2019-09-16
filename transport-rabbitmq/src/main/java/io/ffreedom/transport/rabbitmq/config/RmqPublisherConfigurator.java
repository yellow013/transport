package io.ffreedom.transport.rabbitmq.config;

import javax.annotation.Nonnull;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.MessageProperties;

import io.ffreedom.transport.rabbitmq.declare.BaseEntity.Queue;
import io.ffreedom.transport.rabbitmq.declare.ExchangeDeclare;

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
	// 是否进行发表确认
	private boolean isConfirm;
	// 发布确认超时时间
	private long confirmTimeout;
	// 发布确认重试次数
	private int confirmRetry;

	private RmqPublisherConfigurator(Builder builder) {
		super(builder.connectionConfigurator);
		this.exchangeDeclare = builder.exchangeDeclare;
		this.defaultRoutingKey = builder.defaultRoutingKey;
		this.msgProperties = builder.msgProperties;
		this.isConfirm = builder.isConfirm;
		this.confirmTimeout = builder.confirmTimeout;
		this.confirmRetry = builder.confirmRetry;
	}

	public static Builder configuration(@Nonnull ConnectionConfigurator connectionConfigurator,
			@Nonnull ExchangeDeclare exchangeDeclare) {
		return new Builder(connectionConfigurator, exchangeDeclare);
	}

	/**
	 * @return the exchangeDeclare
	 */
	public ExchangeDeclare getExchangeDeclare() {
		return exchangeDeclare;
	}

	/**
	 * @return the defaultRoutingKey
	 */
	public String getDefaultRoutingKey() {
		return defaultRoutingKey;
	}

	/**
	 * @return the msgProperties
	 */
	public BasicProperties getMsgProperties() {
		return msgProperties;
	}

	/**
	 * @return the isConfirm
	 */
	public boolean isConfirm() {
		return isConfirm;
	}

	/**
	 * @return the confirmTimeout
	 */
	public long getConfirmTimeout() {
		return confirmTimeout;
	}

	/**
	 * @return the confirmRetry
	 */
	public int getConfirmRetry() {
		return confirmRetry;
	}

	public static class Builder {

		// 连接配置
		private ConnectionConfigurator connectionConfigurator;

		private ExchangeDeclare exchangeDeclare;

		private String defaultRoutingKey = "";
		private BasicProperties msgProperties = MessageProperties.PERSISTENT_BASIC;

		private boolean isConfirm = false;
		private long confirmTimeout = 5000;
		private int confirmRetry = 3;

		public Builder(ConnectionConfigurator connectionConfigurator, ExchangeDeclare exchangeDeclare) {
			super();
			this.connectionConfigurator = connectionConfigurator;
			this.exchangeDeclare = exchangeDeclare;
		}

		public RmqPublisherConfigurator build() {
			return new RmqPublisherConfigurator(this);
		}

		/**
		 * @param defaultRoutingKey the defaultRoutingKey to set
		 */
		public Builder setDefaultRoutingKey(String defaultRoutingKey) {
			this.defaultRoutingKey = defaultRoutingKey;
			return this;
		}

		/**
		 * @param msgProperties the msgProperties to set
		 */
		public Builder setMsgProperties(BasicProperties msgProperties) {
			this.msgProperties = msgProperties;
			return this;
		}

		/**
		 * @param isConfirm the isConfirm to set
		 */
		public Builder setConfirm(boolean isConfirm) {
			this.isConfirm = isConfirm;
			return this;
		}

		/**
		 * @param confirmTimeout the confirmTimeout to set
		 */
		public Builder setConfirmTimeout(long confirmTimeout) {
			this.confirmTimeout = confirmTimeout;
			return this;
		}

		/**
		 * @param confirmRetry the confirmRetry to set
		 */
		public Builder setConfirmRetry(int confirmRetry) {
			this.confirmRetry = confirmRetry;
			return this;
		}

	}

	public static void main(String[] args) {

		configuration(ConnectionConfigurator.configuration("", 0, "", "").build(),
				ExchangeDeclare.directExchange("TEST").declareBindingQueue(Queue.declare("TEST_0"))).build();

	}

}
