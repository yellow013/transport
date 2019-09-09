package io.ffreedom.transport.rabbitmq.declare;

import static java.lang.String.valueOf;

public class BaseEntity {

	public static class Exchange {

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

		public static Exchange declareFanout(String name) {
			return new Exchange(ExchangeType.Fanout, name);
		}

		public static Exchange declareDirect(String name) {
			return new Exchange(ExchangeType.Direct, name);
		}

		public static Exchange declareTopic(String name) {
			return new Exchange(ExchangeType.Topic, name);
		}

		private Exchange(ExchangeType type, String name) {
			super();
			this.type = type;
			this.name = name;
		}

		/**
		 * @return the type
		 */
		public ExchangeType getType() {
			return type;
		}

		/**
		 * @return the name
		 */
		public String getName() {
			return name;
		}

		/**
		 * @return the durable
		 */
		public boolean isDurable() {
			return durable;
		}

		/**
		 * @return the autoDelete
		 */
		public boolean isAutoDelete() {
			return autoDelete;
		}

		/**
		 * @return the internal
		 */
		public boolean isInternal() {
			return internal;
		}

		/**
		 * @param durable the durable to set
		 */
		public Exchange setDurable(boolean durable) {
			this.durable = durable;
			return this;
		}

		/**
		 * @param autoDelete the autoDelete to set
		 */
		public Exchange setAutoDelete(boolean autoDelete) {
			this.autoDelete = autoDelete;
			return this;
		}

		/**
		 * @param internal the internal to set
		 */
		public Exchange setInternal(boolean internal) {
			this.internal = internal;
			return this;
		}

		private final static String ToStringTemplate = "Exchange([name=$name],[type=$type],[durable=$durable],[autoDelete=$autoDelete],[internal=$internal])";

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

	public static class Queue {

		private String name;
		// 是否持久化
		private boolean durable = true;
		// 连接独占此队列
		private boolean exclusive = false;
		// channel关闭后自动删除队列
		private boolean autoDelete = false;

		public static Queue declare(String name) {
			return new Queue(name);
		}

		private Queue(String name) {
			super();
			this.name = name;
		}

		/**
		 * @return the name
		 */
		public String getName() {
			return name;
		}

		/**
		 * @return the durable
		 */
		public boolean isDurable() {
			return durable;
		}

		/**
		 * @return the exclusive
		 */
		public boolean isExclusive() {
			return exclusive;
		}

		/**
		 * @return the autoDelete
		 */
		public boolean isAutoDelete() {
			return autoDelete;
		}

		/**
		 * @param durable the durable to set
		 */
		public Queue setDurable(boolean durable) {
			this.durable = durable;
			return this;
		}

		/**
		 * @param exclusive the exclusive to set
		 */
		public Queue setExclusive(boolean exclusive) {
			this.exclusive = exclusive;
			return this;
		}

		/**
		 * @param autoDelete the autoDelete to set
		 */
		public Queue setAutoDelete(boolean autoDelete) {
			this.autoDelete = autoDelete;
			return this;
		}

		private final static String ToStringTemplate = "Queue([name=$name],[durable=$durable],[exclusive=$exclusive],[autoDelete=$autoDelete])";

		@Override
		public String toString() {
			return ToStringTemplate.replace("$name", name).replace("$durable", valueOf(durable))
					.replace("$exclusive", valueOf(exclusive)).replace("$autoDelete", valueOf(autoDelete));
		}

	}

	public static class Binding {

		private Exchange source;
		private Exchange destExchange;
		private Queue destQueue;
		private String routingKey = "";
		private DestinationType destinationType;

		public Binding(Exchange source, Exchange destExchange) {
			super();
			this.source = source;
			this.destExchange = destExchange;
			this.destinationType = DestinationType.Exchange;
		}

		public Binding(Exchange source, Queue destQueue) {
			super();
			this.source = source;
			this.destQueue = destQueue;
			this.destinationType = DestinationType.Queue;
		}

		public Binding(Exchange source, Exchange destExchange, String routingKey) {
			super();
			this.source = source;
			this.destExchange = destExchange;
			this.routingKey = routingKey;
			this.destinationType = DestinationType.Exchange;
		}

		public Binding(Exchange source, Queue destQueue, String routingKey) {
			super();
			this.source = source;
			this.destQueue = destQueue;
			this.routingKey = routingKey;
			this.destinationType = DestinationType.Queue;
		}

		/**
		 * @return the source
		 */
		public Exchange getSource() {
			return source;
		}

		/**
		 * @return the routingKey
		 */
		public String getRoutingKey() {
			return routingKey;
		}

		/**
		 * @return the destExchange
		 */
		public Exchange getDestExchange() {
			return destExchange;
		}

		/**
		 * @return the destQueue
		 */
		public Queue getDestQueue() {
			return destQueue;
		}

		/**
		 * @return the destinationType
		 */
		public DestinationType getDestinationType() {
			return destinationType;
		}
	}

	public static enum ExchangeType {
		Direct, Fanout, Topic
	}

	public static enum DestinationType {
		Exchange, Queue
	}

	public static void main(String[] args) {

		Exchange exchange0 = Exchange.declareDirect("ABC");
		Exchange exchange1 = Exchange.declareDirect("ABC");
		System.out.println(exchange0);
		System.out.println(exchange1);
		System.out.println(exchange0 == exchange1);
		System.out.println(exchange0.idempotent(exchange1));

		System.out.println(Exchange.declareDirect("ABC"));
		System.out.println(Queue.declare("ABC"));

	}

}
