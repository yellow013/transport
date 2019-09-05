package io.ffreedom.transport.rabbitmq.config;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.MessageProperties;

import io.ffreedom.common.utils.StringUtil;

/**
 * 
 * @author yellow013
 * 
 *         TODO 扩展针对多个routingKey的绑定关系
 */
public final class PublisherConfigurator {

	/**
	 * 发布者参数
	 */
	private String exchange = "";
	private String routingKey = "";
	private String[] bindQueues = null;
	private BasicProperties msgProperties = MessageProperties.PERSISTENT_BASIC;
	private BuiltinExchangeType builtinExchangeType = BuiltinExchangeType.FANOUT;
	// 是否持久化
	private boolean durable = true;
	// 没有使用时自动删除
	private boolean autoDelete = false;
	// 是否为内部Exchange
	private boolean internal = false;
	// 连接独占此队列(针对绑定的队列)
	private boolean exclusive = false;

	private boolean isConfirm = false;
	private long confirmTimeout = 5000;
	private int confirmRetry = 3;

	//连接配置
	private ConnectionConfigurator connectionConfigurator;

	private PublisherConfigurator(ConnectionConfigurator connectionConfigurator) {
		this.connectionConfigurator = connectionConfigurator;
	}

	public static PublisherConfigurator configuration(String host, int port, String username, String password) {
		return new PublisherConfigurator(ConnectionConfigurator.configuration(host, port, username, password));
	}

	public static PublisherConfigurator configuration(String host, int port, String username, String password,
			String virtualHost) {
		return new PublisherConfigurator(
				ConnectionConfigurator.configuration(host, port, username, password, virtualHost));
	}

	public static PublisherConfigurator configuration(ConnectionConfigurator connectionConfigurator) {
		return new PublisherConfigurator(connectionConfigurator);
	}

	public ConnectionConfigurator getConnectionConfigurator() {
		return connectionConfigurator;
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

	public BuiltinExchangeType getBuiltinExchangeType() {
		return builtinExchangeType;
	}

	public boolean isDurable() {
		return durable;
	}

	public boolean isAutoDelete() {
		return autoDelete;
	}

	public boolean isInternal() {
		return internal;
	}

	public boolean isExclusive() {
		return exclusive;
	}

	public boolean isConfirm() {
		return isConfirm;
	}

	public long getConfirmTimeout() {
		return confirmTimeout;
	}

	public int getConfirmRetry() {
		return confirmRetry;
	}

	public void setDurable(boolean durable) {
		this.durable = durable;
	}

	public void setAutoDelete(boolean autoDelete) {
		this.autoDelete = autoDelete;
	}

	public void setInternal(boolean internal) {
		this.internal = internal;
	}

	public void setExclusive(boolean exclusive) {
		this.exclusive = exclusive;
	}

	public void setConfirm(boolean isConfirm) {
		this.isConfirm = isConfirm;
	}

	public PublisherConfigurator setConfirmTimeout(long confirmTimeout) {
		this.confirmTimeout = confirmTimeout;
		return this;
	}

	public PublisherConfigurator setConfirmRetry(int confirmRetry) {
		this.confirmRetry = confirmRetry;
		return this;
	}

	public PublisherConfigurator setMsgProperties(BasicProperties msgProperties) {
		this.msgProperties = msgProperties;
		return this;
	}

	public PublisherConfigurator setFanoutExchange(String exchange) {
		return setFanoutExchangeAndBindQueues(exchange, null);
	}

	public PublisherConfigurator setFanoutExchangeAndBindQueues(String exchange, String[] bindQueues) {
		return setExchange(ExchangeType.FANOUT, exchange, null, bindQueues);
	}

	public PublisherConfigurator setDirectExchange(String exchange) {
		return setDirectExchange(exchange, null);
	}

	public PublisherConfigurator setDirectExchange(String exchange, String routingKey) {
		return setDirectExchangeAndBindQueues(exchange, routingKey, null);
	}

	public PublisherConfigurator setDirectExchangeAndBindQueues(String exchange, String[] bindQueues) {
		return setDirectExchangeAndBindQueues(exchange, bindQueues);
	}

	public PublisherConfigurator setDirectExchangeAndBindQueues(String exchange, String routingKey,
			String[] bindQueues) {
		return setExchange(ExchangeType.DIRECT, exchange, routingKey, bindQueues);
	}

	public PublisherConfigurator setModeTopic(String exchange, String routingKey, String[] bindQueues) {
		return setExchange(ExchangeType.FANOUT, exchange, routingKey, bindQueues);
	}

	private PublisherConfigurator setExchange(ExchangeType exchangeType, String exchange, String routingKey,
			String[] bindQueues) {
		if (StringUtil.isNullOrEmpty(exchange))
			throw new IllegalArgumentException("Param exchange not allowed null");
		// 设置exchange
		this.exchange = exchange;
		// 设置routingKey
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
			throw new IllegalArgumentException("exchangeType is error : " + exchangeType);
		}
	}

	private enum ExchangeType {
		DIRECT, FANOUT, TOPIC
	}

}
