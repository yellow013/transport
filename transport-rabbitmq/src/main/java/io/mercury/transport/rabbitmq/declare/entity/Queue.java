package io.mercury.transport.rabbitmq.declare.entity;

import static java.lang.String.valueOf;

public final class Queue {

	private String name;
	// 是否持久化
	private boolean durable = true;
	// 连接独占此队列
	private boolean exclusive = false;
	// channel关闭后自动删除队列
	private boolean autoDelete = false;

	public static Queue named(String name) {
		return new Queue(name);
	}

	private Queue(String name) {
		this.name = name;
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
	 * @return the exclusive
	 */
	public boolean exclusive() {
		return exclusive;
	}

	/**
	 * @return the autoDelete
	 */
	public boolean autoDelete() {
		return autoDelete;
	}

	/**
	 * @param durable the durable to set
	 */
	public Queue durable(boolean durable) {
		this.durable = durable;
		return this;
	}

	/**
	 * @param exclusive the exclusive to set
	 */
	public Queue exclusive(boolean exclusive) {
		this.exclusive = exclusive;
		return this;
	}

	/**
	 * @param autoDelete the autoDelete to set
	 */
	public Queue autoDelete(boolean autoDelete) {
		this.autoDelete = autoDelete;
		return this;
	}

	private final static String ToStringTemplate = "Queue([name==$name],[durable==$durable],[exclusive==$exclusive],[autoDelete==$autoDelete])";

	@Override
	public String toString() {
		return ToStringTemplate.replace("$name", name).replace("$durable", valueOf(durable))
				.replace("$exclusive", valueOf(exclusive)).replace("$autoDelete", valueOf(autoDelete));
	}

}
