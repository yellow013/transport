package io.mercury.transport.rabbitmq.config;

import javax.annotation.Nonnull;

import io.mercury.common.utils.StringUtil;
import io.mercury.transport.rabbitmq.declare.ExchangeDeclare;
import io.mercury.transport.rabbitmq.declare.QueueDeclare;

public final class RmqReceiverConfigurator extends RmqConfigurator {

	// 接受者QueueDeclare
	private QueueDeclare queueDeclare;
	// 错误消息ExchangeDeclare
	private ExchangeDeclare errorMsgExchange;
	// 自动ACK
	private boolean autoAck;
	// 一次ACK多条
	private boolean multipleAck;
	// 最大重新ACK次数
	private int maxAckTotal;
	// 最大ACK重连次数
	private int maxAckReconnection;
	// QOS预取
	private int qos;

	private RmqReceiverConfigurator(Builder builder) {
		super(builder.connection);
		this.queueDeclare = builder.queueDeclare;
		this.errorMsgExchange = builder.errorMsgExchange;
		this.autoAck = builder.autoAck;
		this.multipleAck = builder.multipleAck;
		this.maxAckTotal = builder.maxAckTotal;
		this.maxAckReconnection = builder.maxAckReconnection;
		this.qos = builder.qos;
	}

	public static Builder configuration(@Nonnull ConnectionConfigurator connection,
			@Nonnull QueueDeclare queueDeclare) {
		return new Builder(connection, queueDeclare);
	}

	/**
	 * @return the queueDeclare
	 */
	public QueueDeclare queueDeclare() {
		return queueDeclare;
	}

	/**
	 * @return the errorMsgExchange
	 */
	public ExchangeDeclare errorMsgExchange() {
		return errorMsgExchange;
	}

	/**
	 * @return the isAutoAck
	 */
	public boolean autoAck() {
		return autoAck;
	}

	/**
	 * @return the isMultipleAck
	 */
	public boolean multipleAck() {
		return multipleAck;
	}

	/**
	 * @return the maxAckTotal
	 */
	public int maxAckTotal() {
		return maxAckTotal;
	}

	/**
	 * @return the maxAckReconnection
	 */
	public int maxAckReconnection() {
		return maxAckReconnection;
	}

	/**
	 * @return the qos
	 */
	public int qos() {
		return qos;
	}

	private String ToStringStr;

	@Override
	public String toString() {
		if (ToStringStr == null)
			ToStringStr = StringUtil.reflectionToString(this);
		return ToStringStr;
	}

	public static class Builder {
		// 连接配置
		private ConnectionConfigurator connection;
		// 接受者QueueDeclare
		private QueueDeclare queueDeclare;
		// 错误消息ExchangeDeclare
		private ExchangeDeclare errorMsgExchange;
		// 自动ACK
		private boolean autoAck = true;
		// 一次ACK多条
		private boolean multipleAck = false;
		// 最大重新ACK次数
		private int maxAckTotal = 16;
		// 最大ACK重连次数
		private int maxAckReconnection = 8;
		// QOS预取
		private int qos = 256;

		private Builder(ConnectionConfigurator connection, QueueDeclare queueDeclare) {
			this.connection = connection;
			this.queueDeclare = queueDeclare;
		}

		/**
		 * @param errorMsgExchange the errorMsgExchange to set
		 */
		public Builder errorMsgExchange(ExchangeDeclare errorMsgExchange) {
			this.errorMsgExchange = errorMsgExchange;
			return this;
		}

		/**
		 * @param isAutoAck the isAutoAck to set
		 */
		public Builder autoAck(boolean autoAck) {
			this.autoAck = autoAck;
			return this;
		}

		/**
		 * @param isMultipleAck the isMultipleAck to set
		 */
		public Builder multipleAck(boolean multipleAck) {
			this.multipleAck = multipleAck;
			return this;
		}

		/**
		 * @param maxAckTotal the maxAckTotal to set
		 */
		public Builder maxAckTotal(int maxAckTotal) {
			this.maxAckTotal = maxAckTotal;
			return this;
		}

		/**
		 * @param maxAckReconnection the maxAckReconnection to set
		 */
		public Builder maxAckReconnection(int maxAckReconnection) {
			this.maxAckReconnection = maxAckReconnection;
			return this;
		}

		/**
		 * @param qos the qos to set
		 */
		public Builder qos(int qos) {
			this.qos = qos;
			return this;
		}

		public RmqReceiverConfigurator build() {
			return new RmqReceiverConfigurator(this);
		}

	}

}
