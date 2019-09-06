package io.ffreedom.transport.rabbitmq.config;

import javax.annotation.Nonnull;

public final class ReceiverConfigurator {

	/**
	 * 接收者参数
	 */
	// 接受者队列
	private String receiveQueue;
	// 需要绑定的Exchange
	private String exchange[];
	// 需要绑定的routingKey
	private String routingKey[];

	// 错误消息Ecchange
	private String errorMsgToExchange;
	// 是否持久化
	private boolean durable = true;
	// 连接独占此队列
	private boolean exclusive = false;
	// channel关闭后自动删除队列
	private boolean autoDelete = false;
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

	// 连接配置
	private ConnectionConfigurator connectionConfigurator;

	private ReceiverConfigurator(ConnectionConfigurator connectionConfigurator) {
		this.connectionConfigurator = connectionConfigurator;
	}

	public static ReceiverConfigurator configuration(@Nonnull ConnectionConfigurator connectionConfigurator) {
		return new ReceiverConfigurator(connectionConfigurator);
	}

	public ConnectionConfigurator getConnectionConfigurator() {
		return connectionConfigurator;
	}

	public String[] getExchange() {
		return exchange;
	}

	public String[] getRoutingKey() {
		return routingKey;
	}

	public String getReceiveQueue() {
		return receiveQueue;
	}

	public String getErrorMsgToExchange() {
		return errorMsgToExchange;
	}

	public boolean isDurable() {
		return durable;
	}

	public boolean isExclusive() {
		return exclusive;
	}

	public boolean isAutoDelete() {
		return autoDelete;
	}

	public boolean isAutoAck() {
		return isAutoAck;
	}

	public boolean isMultipleAck() {
		return isMultipleAck;
	}

	public int getMaxAckTotal() {
		return maxAckTotal;
	}

	public int getMaxAckReconnection() {
		return maxAckReconnection;
	}

	public int getQos() {
		return qos;
	}

	public ReceiverConfigurator setExchange(String... exchange) {
		this.exchange = exchange;
		return this;
	}

	public void setRoutingKey(String... routingKey) {
		this.routingKey = routingKey;
	}

	public ReceiverConfigurator setReceiveQueue(String receiveQueue) {
		this.receiveQueue = receiveQueue;
		return this;
	}

	public ReceiverConfigurator setErrorMsgToExchange(String errorMsgToExchange) {
		this.errorMsgToExchange = errorMsgToExchange;
		return this;
	}

	public ReceiverConfigurator setDurable(boolean durable) {
		this.durable = durable;
		return this;
	}

	public ReceiverConfigurator setExclusive(boolean exclusive) {
		this.exclusive = exclusive;
		return this;
	}

	public ReceiverConfigurator setAutoDelete(boolean autoDelete) {
		this.autoDelete = autoDelete;
		return this;
	}

	public ReceiverConfigurator setAutoAck(boolean isAutoAck) {
		this.isAutoAck = isAutoAck;
		return this;
	}

	public ReceiverConfigurator setMultipleAck(boolean isMultipleAck) {
		this.isMultipleAck = isMultipleAck;
		return this;
	}

	public ReceiverConfigurator setMaxAckTotal(int maxAckTotal) {
		this.maxAckTotal = maxAckTotal;
		return this;
	}

	public ReceiverConfigurator setMaxAckReconnection(int maxAckReconnection) {
		this.maxAckReconnection = maxAckReconnection;
		return this;
	}

	public ReceiverConfigurator setQos(int qos) {
		this.qos = qos;
		return this;
	}

}
