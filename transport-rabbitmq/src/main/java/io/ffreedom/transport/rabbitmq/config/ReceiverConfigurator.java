package io.ffreedom.transport.rabbitmq.config;

import io.ffreedom.common.functional.ShutdownEvent;

import javax.net.ssl.SSLContext;

public final class ReceiverConfigurator extends ConnectionConfigurator {

	/**
	 * 接收者参数
	 */
	private String exchange;
	private String receiveQueue;
	private String errorMsgToExchange;
	// 自动ACK
	private boolean isAutoAck = true;
	// 一次ACK多条
	private boolean isMultipleAck = false;

	// 最大重新ACK次数
	private int maxAckTotal = 16;
	// 最大ACK重连次数
	private int maxAckReconnection = 8;

	private int qos = 1024;

//	private ReceiverConfigurator(String host, int port) {
//		super("RabbitMqReceiverConfigurator", host, port);
//	}
	
	private ReceiverConfigurator(String host, int port, String username, String password) {
		super("RabbitMqReceiverConfigurator", host, port);
		authenticates(username, password);
	}

//	public static ReceiverConfigurator configuration(String host, int port) {
//		return new ReceiverConfigurator(host, port);
//	}
	
	public static ReceiverConfigurator configuration(String host, int port, String username, String password) {
		return new ReceiverConfigurator(host, port, username, password);
	}

	public String getExchange() {
		return exchange;
	}

	public ReceiverConfigurator setExchange(String exchange) {
		this.exchange = exchange;
		return this;
	}

	public String getErrorMsgToExchange() {
		return errorMsgToExchange;
	}

	public ReceiverConfigurator setErrorMsgToExchange(String errorMsgToExchange) {
		this.errorMsgToExchange = errorMsgToExchange;
		return this;
	}

	public String getReceiveQueue() {
		return receiveQueue;
	}

	public ReceiverConfigurator setReceiveQueue(String receiveQueue) {
		this.receiveQueue = receiveQueue;
		return this;
	}

	public boolean isAutoAck() {
		return isAutoAck;
	}

	public ReceiverConfigurator setAutoAck(boolean isAutoAck) {
		this.isAutoAck = isAutoAck;
		return this;
	}

	public boolean isMultipleAck() {
		return isMultipleAck;
	}

	public ReceiverConfigurator setMultipleAck(boolean isMultipleAck) {
		this.isMultipleAck = isMultipleAck;
		return this;
	}

	public int getMaxAckTotal() {
		return maxAckTotal;
	}

	public ReceiverConfigurator setMaxAckTotal(int maxAckTotal) {
		this.maxAckTotal = maxAckTotal;
		return this;
	}

	public int getMaxAckReconnection() {
		return maxAckReconnection;
	}

	public ReceiverConfigurator setMaxAckReconnection(int maxAckReconnection) {
		this.maxAckReconnection = maxAckReconnection;
		return this;
	}

	public int getQos() {
		return qos;
	}

	public ReceiverConfigurator setQos(int qos) {
		this.qos = qos;
		return this;
	}

	/**
	 * 配置连接信息 START
	 */
	private ReceiverConfigurator authenticates(String username, String password) {
		this.username = username;
		this.password = password;
		return this;
	}

	public ReceiverConfigurator setSslContext(SSLContext sslContext) {
		this.sslContext = sslContext;
		return this;
	}

	public ReceiverConfigurator setConnectionTimeout(int connectionTimeout) {
		this.connectionTimeout = connectionTimeout;
		return this;
	}

	public ReceiverConfigurator setVirtualHost(String virtualHost) {
		this.virtualHost = virtualHost;
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

	public ReceiverConfigurator setAutomaticRecovery(boolean automaticRecovery) {
		this.automaticRecovery = automaticRecovery;
		return this;
	}

	public ReceiverConfigurator setRecoveryInterval(long recoveryInterval) {
		this.recoveryInterval = recoveryInterval;
		return this;
	}

	public ReceiverConfigurator setHandshakeTimeout(int handshakeTimeout) {
		this.handshakeTimeout = handshakeTimeout;
		return this;
	}

	public ReceiverConfigurator setShutdownTimeout(int shutdownTimeout) {
		this.shutdownTimeout = shutdownTimeout;
		return this;
	}

	public ReceiverConfigurator setRequestedHeartbeat(int requestedHeartbeat) {
		this.requestedHeartbeat = requestedHeartbeat;
		return this;
	}

	public ReceiverConfigurator setShutdownEvent(ShutdownEvent<Exception> shutdownEvent) {
		this.shutdownEvent = shutdownEvent;
		return this;
	}

	/**
	 * 配置连接信息 END
	 */
}
