package io.mercury.transport.rabbitmq.declare.entity;

import static java.lang.String.valueOf;

public final class Exchange {

	public static enum ExchangeType {
		Direct, Fanout, Topic, Anonymous
	}

	// ExchangeType
	private ExchangeType type;
	// name
	private String name;
	// 是否持久化
	private boolean durable = true;
	// 没有使用时自动删除
	private boolean autoDelete = false;
	// 是否为内部Exchange
	private boolean internal = false;

	public static Exchange fanout(String name) {
		return new Exchange(ExchangeType.Fanout, name);
	}

	public static Exchange direct(String name) {
		return new Exchange(ExchangeType.Direct, name);
	}

	public static Exchange topic(String name) {
		return new Exchange(ExchangeType.Topic, name);
	}

	public static Exchange anonymous() {
		return new Exchange(ExchangeType.Anonymous, "");
	}

	private Exchange(ExchangeType type, String name) {
		this.type = type;
		this.name = name;
	}

	/**
	 * @return the type
	 */
	public ExchangeType type() {
		return type;
	}

	/**
	 * @return the name
	 */
	public String name() {
		return name;
	}

	/**
	 * @return the durable
	 */
	public boolean durable() {
		return durable;
	}

	/**
	 * @return the autoDelete
	 */
	public boolean autoDelete() {
		return autoDelete;
	}

	/**
	 * @return the internal
	 */
	public boolean internal() {
		return internal;
	}

	/**
	 * @param durable the durable to set
	 */
	public Exchange durable(boolean durable) {
		this.durable = durable;
		return this;
	}

	/**
	 * @param autoDelete the autoDelete to set
	 */
	public Exchange autoDelete(boolean autoDelete) {
		this.autoDelete = autoDelete;
		return this;
	}

	/**
	 * @param internal the internal to set
	 */
	public Exchange internal(boolean internal) {
		this.internal = internal;
		return this;
	}

	private final static String ToStringTemplate = "Exchange([name==$name],[type==$type],[durable==$durable],[autoDelete==$autoDelete],[internal==$internal])";

	@Override
	public String toString() {
		return ToStringTemplate.replace("$name", name).replace("$type", valueOf(type))
				.replace("$durable", valueOf(durable)).replace("$autoDelete", valueOf(autoDelete))
				.replace("$internal", valueOf(internal));
	}

	public boolean idempotent(Exchange another) {
		return name.equals(another.name) && type == another.type && durable == another.durable
				&& autoDelete == another.autoDelete && internal == another.internal;
	}

}
