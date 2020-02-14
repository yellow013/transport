package io.mercury.transport.rabbitmq.configurator;

import javax.annotation.Nonnull;

import io.mercury.common.util.Assertor;
import io.mercury.common.util.StringUtil;
import io.mercury.transport.rabbitmq.declare.ExchangeRelation;
import io.mercury.transport.rabbitmq.declare.QueueRelation;

public final class RmqReceiverConfigurator extends RmqConfigurator {

	// 接受者QueueDeclare
	private QueueRelation receiveQueue;

	// 错误消息ExchangeDeclare
	private ExchangeRelation errorMsgExchange;

	// 错误消息RoutingKey
	private String errorMsgRoutingKey;

	// 错误消息QueueDeclare
	private QueueRelation errorMsgQueue;

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
		this.receiveQueue = builder.receiveQueue;
		this.errorMsgExchange = builder.errorMsgExchange;
		this.errorMsgRoutingKey = builder.errorMsgRoutingKey;
		this.errorMsgQueue = builder.errorMsgQueue;
		this.autoAck = builder.autoAck;
		this.multipleAck = builder.multipleAck;
		this.maxAckTotal = builder.maxAckTotal;
		this.maxAckReconnection = builder.maxAckReconnection;
		this.qos = builder.qos;
	}

	public static Builder configuration(@Nonnull RmqConnection connection, @Nonnull QueueRelation queueDeclare) {
		return new Builder(Assertor.nonNull(connection, "connection"), Assertor.nonNull(queueDeclare, "queueDeclare"));
	}

	/**
	 * @return the queueDeclare
	 */
	public QueueRelation receiveQueue() {
		return receiveQueue;
	}

	/**
	 * @return the errorMsgExchange
	 */
	public ExchangeRelation errorMsgExchange() {
		return errorMsgExchange;
	}

	/**
	 * 
	 * @return the errorMsgRoutingKey
	 */

	public String errorMsgRoutingKey() {
		return errorMsgRoutingKey;
	}

	/**
	 * 
	 * @return the errorMsgQueue
	 */
	public QueueRelation errorMsgQueue() {
		return errorMsgQueue;
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
		// 接受者QueueDeclare
		private QueueRelation receiveQueue;
		// 错误消息ExchangeDeclare
		private ExchangeRelation errorMsgExchange;
		// 错误消息RoutingKey
		private String errorMsgRoutingKey = "";
		// 错误消息QueueDeclare
		private QueueRelation errorMsgQueue;
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

		private Builder(RmqConnection connection, QueueRelation receiveQueue) {
			this.connection = connection;
			this.receiveQueue = receiveQueue;
		}

		/**
		 * @param errorMsgExchange the errorMsgExchange to set
		 */
		public Builder errorMsgExchange(ExchangeRelation errorMsgExchange) {
			this.errorMsgExchange = errorMsgExchange;
			return this;
		}

		/**
		 * 
		 * @param errorMsgRoutingKey
		 */
		public Builder errorMsgRoutingKey(String errorMsgRoutingKey) {
			this.errorMsgRoutingKey = errorMsgRoutingKey;
			return this;
		}

		/**
		 * 
		 * @param errorMsgQueue
		 */
		public Builder errorMsgQueue(QueueRelation errorMsgQueue) {
			this.errorMsgQueue = errorMsgQueue;
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
