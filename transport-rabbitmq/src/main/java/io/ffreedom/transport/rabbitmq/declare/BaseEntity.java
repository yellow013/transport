package io.ffreedom.transport.rabbitmq.declare;

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

	}

	public static class Binding {

		private Exchange source;
		private Exchange destinationExchange;
		private Queue destinationQueue;
		private String routingKey = "";
		private DestinationType destinationType;

		public Binding(Exchange source, Exchange destinationExchange) {
			super();
			this.source = source;
			this.destinationExchange = destinationExchange;
			this.destinationType = DestinationType.Exchange;
		}

		public Binding(Exchange source, Queue destinationQueue) {
			super();
			this.source = source;
			this.destinationQueue = destinationQueue;
			this.destinationType = DestinationType.Queue;
		}

		public Binding(Exchange source, Exchange destinationExchange, String routingKey) {
			super();
			this.source = source;
			this.destinationExchange = destinationExchange;
			this.routingKey = routingKey;
			this.destinationType = DestinationType.Exchange;
		}

		public Binding(Exchange source, Queue destinationQueue, String routingKey) {
			super();
			this.source = source;
			this.destinationQueue = destinationQueue;
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
		 * @return the destinationExchange
		 */
		public Exchange getDestinationExchange() {
			return destinationExchange;
		}

		/**
		 * @return the destinationQueue
		 */
		public Queue getDestinationQueue() {
			return destinationQueue;
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

}
