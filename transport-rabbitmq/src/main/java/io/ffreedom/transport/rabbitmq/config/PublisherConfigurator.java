package io.ffreedom.transport.rabbitmq.config;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.MessageProperties;

import io.ffreedom.common.functional.ShutdownEvent;
import io.ffreedom.common.utils.StringUtil;

import javax.net.ssl.SSLContext;

public class PublisherConfigurator extends ConnectionConfigurator {

	/**
	 * 发布者参数
	 */
	private String exchange = "";
	private String routingKey = "";
	private String[] bindQueues = null;
	private BasicProperties msgProperties = MessageProperties.PERSISTENT_BASIC;
	private BuiltinExchangeType builtinExchangeType = BuiltinExchangeType.DIRECT;
	private boolean isConfirm = false;
	private long confirmTimeout = 5000;
	private int confirmRetry = 3;

	private PublisherConfigurator(String host, int port) {
		super("RabbitMqPublisherConfigurator", host, port);
	}

	private PublisherConfigurator(String host, int port, String username, String password) {
		super("RabbitMqPublisherConfigurator", host, port);
		authenticates(username, password);
	}

	public static PublisherConfigurator configuration(String host, int port) {
		return new PublisherConfigurator(host, port);
	}

	public static PublisherConfigurator configuration(String host, int port, String username, String password) {
		return new PublisherConfigurator(host, port, username, password);
	}

	public String getExchange() {
		return exchange;
	}

	public String getRoutingKey() {
		return routingKey;
	}

	public String[] getBindQueues() {
		return bindQueues;
	}

	public BasicProperties getMsgProperties() {
		return msgProperties;
	}

	public PublisherConfigurator setMsgProperties(BasicProperties msgProperties) {
		this.msgProperties = msgProperties;
		return this;
	}

	public boolean isConfirm() {
		return isConfirm;
	}

	public PublisherConfigurator openConfirm() {
		this.isConfirm = true;
		return this;
	}

	public PublisherConfigurator closeConfirm() {
		this.isConfirm = false;
		return this;
	}

	public long getConfirmTimeout() {
		return confirmTimeout;
	}

	public PublisherConfigurator setConfirmTimeout(long confirmTimeout) {
		this.confirmTimeout = confirmTimeout;
		return this;
	}

	public int getConfirmRetry() {
		return confirmRetry;
	}

	public PublisherConfigurator setConfirmRetry(int confirmRetry) {
		this.confirmRetry = confirmRetry;
		return this;
	}

	public BuiltinExchangeType getBuiltinExchangeType() {
		return builtinExchangeType;
	}

	public PublisherConfigurator setFanoutExchange(String exchange, String[] bindQueues) {
		return setMode(ExchangeType.FANOUT, exchange, null, bindQueues);
	}

	public PublisherConfigurator setDirectExchange(String exchange, String routingKey, String[] bindQueues) {
		return setMode(ExchangeType.DIRECT, exchange, routingKey, bindQueues);
	}

	private PublisherConfigurator setMode(ExchangeType exchangeType, String exchange, String routingKey,
			String[] bindQueues) {
		if (StringUtil.isNullOrEmpty(exchange))
			throw new IllegalArgumentException("Param exchange not allowed null");
		// 设置exchange
		this.exchange = exchange;
		//设置routingKey
		if (!StringUtil.isNullOrEmpty(routingKey))
			this.routingKey = routingKey;
		// 设置需要绑定的Queue
		if (bindQueues != null)
			this.bindQueues = bindQueues;
		switch (exchangeType) {
		case DIRECT:
			this.builtinExchangeType = BuiltinExchangeType.DIRECT;
			return this;
		case FANOUT:
			this.builtinExchangeType = BuiltinExchangeType.FANOUT;
			return this;
		case TOPIC:
			this.builtinExchangeType = BuiltinExchangeType.TOPIC;
			return this;
		default:
			return this;
		}
	}

	public PublisherConfigurator setModeDirect(String directQueue) {
		return setMode(ExchangeType.DIRECT, directQueue);
	}

	public PublisherConfigurator setModeFanout(String exchange) {
		return setMode(ExchangeType.FANOUT, exchange);
	}

	public PublisherConfigurator setModeFanoutAndBindQueues(String exchange, String[] bindQueues) {
		return setMode(ExchangeType.FANOUT, exchange, bindQueues);
	}

	public PublisherConfigurator setModeTopic(String exchange, String routingKey, String[] bindQueues) {
		return setMode(ExchangeType.FANOUT, exchange, routingKey, bindQueues);
	}

	/**
	 * 配置连接信息 START
	 */

	private PublisherConfigurator authenticates(String username, String password) {
		this.username = username;
		this.password = password;
		return this;
	}

	public PublisherConfigurator setSslContext(SSLContext sslContext) {
		this.sslContext = sslContext;
		return this;
	}

	public PublisherConfigurator setConnectionTimeout(int connectionTimeout) {
		this.connectionTimeout = connectionTimeout;
		return this;
	}

	public PublisherConfigurator setVirtualHost(String virtualHost) {
		this.virtualHost = virtualHost;
		return this;
	}

	public PublisherConfigurator setDurable(boolean durable) {
		this.durable = durable;
		return this;
	}

	public PublisherConfigurator setExclusive(boolean exclusive) {
		this.exclusive = exclusive;
		return this;
	}

	public PublisherConfigurator setAutoDelete(boolean autoDelete) {
		this.autoDelete = autoDelete;
		return this;
	}

	public PublisherConfigurator setAutomaticRecovery(boolean automaticRecovery) {
		this.automaticRecovery = automaticRecovery;
		return this;
	}

	public PublisherConfigurator setRecoveryInterval(long recoveryInterval) {
		this.recoveryInterval = recoveryInterval;
		return this;
	}

	public PublisherConfigurator setHandshakeTimeout(int handshakeTimeout) {
		this.handshakeTimeout = handshakeTimeout;
		return this;
	}

	public PublisherConfigurator setShutdownTimeout(int shutdownTimeout) {
		this.shutdownTimeout = shutdownTimeout;
		return this;
	}

	public PublisherConfigurator setRequestedHeartbeat(int requestedHeartbeat) {
		this.requestedHeartbeat = requestedHeartbeat;
		return this;
	}

	public PublisherConfigurator setShutdownEvent(ShutdownEvent<Exception> shutdownEvent) {
		this.shutdownEvent = shutdownEvent;
		return this;
	}

	/**
	 * 配置连接信息 END
	 */

	public static enum ExchangeType {
		DIRECT, FANOUT, TOPIC
	}

}
