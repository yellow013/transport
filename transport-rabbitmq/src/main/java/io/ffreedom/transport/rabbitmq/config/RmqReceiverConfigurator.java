package io.ffreedom.transport.rabbitmq.config;

import javax.annotation.Nonnull;

import io.ffreedom.transport.rabbitmq.declare.ExchangeDeclare;
import io.ffreedom.transport.rabbitmq.declare.QueueDeclare;

public final class RmqReceiverConfigurator extends RmqConfigurator {

	// 接受者QueueDeclare
	private QueueDeclare queueDeclare;
	// 错误消息ExchangeDeclare
	private ExchangeDeclare errorMsgExchange;
	// 自动ACK
	private boolean isAutoAck;
	// 一次ACK多条
	private boolean isMultipleAck;
	// 最大重新ACK次数
	private int maxAckTotal;
	// 最大ACK重连次数
	private int maxAckReconnection;
	// QOS预取
	private int qos;

	private RmqReceiverConfigurator(Builder builder) {
		super(builder.connectionConfigurator);
		this.queueDeclare = builder.queueDeclare;
		this.errorMsgExchange = builder.errorMsgExchange;
		this.isAutoAck = builder.isAutoAck;
		this.isMultipleAck = builder.isMultipleAck;
		this.maxAckTotal = builder.maxAckTotal;
		this.maxAckReconnection = builder.maxAckReconnection;
		this.qos = builder.qos;
	}

	public static Builder configuration(@Nonnull ConnectionConfigurator connectionConfigurator,
			@Nonnull QueueDeclare queueDeclare) {
		return new Builder(connectionConfigurator, queueDeclare);
	}

	/**
	 * @return the queueDeclare
	 */
	public QueueDeclare getQueueDeclare() {
		return queueDeclare;
	}

	/**
	 * @return the errorMsgExchange
	 */
	public ExchangeDeclare getErrorMsgExchange() {
		return errorMsgExchange;
	}

	/**
	 * @return the isAutoAck
	 */
	public boolean isAutoAck() {
		return isAutoAck;
	}

	/**
	 * @return the isMultipleAck
	 */
	public boolean isMultipleAck() {
		return isMultipleAck;
	}

	/**
	 * @return the maxAckTotal
	 */
	public int getMaxAckTotal() {
		return maxAckTotal;
	}

	/**
	 * @return the maxAckReconnection
	 */
	public int getMaxAckReconnection() {
		return maxAckReconnection;
	}

	/**
	 * @return the qos
	 */
	public int getQos() {
		return qos;
	}

	public static class Builder {
		// 连接配置
		private ConnectionConfigurator connectionConfigurator;
		// 接受者QueueDeclare
		private QueueDeclare queueDeclare;
		// 错误消息ExchangeDeclare
		private ExchangeDeclare errorMsgExchange;
		// 自动ACK
		private boolean isAutoAck = true;
		// 一次ACK多条
		private boolean isMultipleAck = false;
		// 最大重新ACK次数
		private int maxAckTotal = 16;
		// 最大ACK重连次数
		private int maxAckReconnection = 8;
		// QOS预取
		private int qos = 256;

		public Builder(ConnectionConfigurator connectionConfigurator, QueueDeclare queueDeclare) {
			this.connectionConfigurator = connectionConfigurator;
			this.queueDeclare = queueDeclare;
		}

		public RmqReceiverConfigurator build() {
			return new RmqReceiverConfigurator(this);
		}

		/**
		 * @param errorMsgExchange the errorMsgExchange to set
		 */
		public Builder setErrorMsgExchange(ExchangeDeclare errorMsgExchange) {
			this.errorMsgExchange = errorMsgExchange;
			return this;
		}

		/**
		 * @param isAutoAck the isAutoAck to set
		 */
		public Builder setAutoAck(boolean isAutoAck) {
			this.isAutoAck = isAutoAck;
			return this;
		}

		/**
		 * @param isMultipleAck the isMultipleAck to set
		 */
		public Builder setMultipleAck(boolean isMultipleAck) {
			this.isMultipleAck = isMultipleAck;
			return this;
		}

		/**
		 * @param maxAckTotal the maxAckTotal to set
		 */
		public Builder setMaxAckTotal(int maxAckTotal) {
			this.maxAckTotal = maxAckTotal;
			return this;
		}

		/**
		 * @param maxAckReconnection the maxAckReconnection to set
		 */
		public Builder setMaxAckReconnection(int maxAckReconnection) {
			this.maxAckReconnection = maxAckReconnection;
			return this;
		}

		/**
		 * @param qos the qos to set
		 */
		public Builder setQos(int qos) {
			this.qos = qos;
			return this;
		}

	}

}
