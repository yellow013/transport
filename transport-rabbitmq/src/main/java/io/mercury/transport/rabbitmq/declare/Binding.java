package io.mercury.transport.rabbitmq.declare;

import io.mercury.common.util.Assertor;

final class Binding {

	static enum DestType {
		Exchange, Queue
	}

	private Exchange source;
	private Exchange destExchange;
	private Queue destQueue;
	private String routingKey = "";
	private DestType destType;

	/**
	 * 
	 * @param source
	 * @param destExchange
	 */
	Binding(Exchange source, Exchange destExchange) {
		this(source, destExchange, null, null, DestType.Exchange);
	}

	/**
	 * 
	 * @param source
	 * @param destQueue
	 */
	Binding(Exchange source, Queue destQueue) {
		this(source, null, destQueue, null, DestType.Queue);
	}

	/**
	 * 
	 * @param source
	 * @param destExchange
	 * @param routingKey
	 */
	Binding(Exchange source, Exchange destExchange, String routingKey) {
		this(source, destExchange, null, routingKey, DestType.Exchange);
	}

	/**
	 * 
	 * @param source
	 * @param destQueue
	 * @param routingKey
	 */
	Binding(Exchange source, Queue destQueue, String routingKey) {
		this(source, null, destQueue, routingKey, DestType.Queue);
	}

	Binding(Exchange source, Exchange destExchange, Queue destQueue, String routingKey, DestType destType) {
		this.source = Assertor.nonNull(source, "source");
		this.destExchange = destExchange;
		this.destQueue = destQueue;
		this.routingKey = routingKey == null ? "" : routingKey;
		this.destType = destType;
	}

	/**
	 * @return the source
	 */
	Exchange source() {
		return source;
	}

	/**
	 * @return the routingKey
	 */
	String routingKey() {
		return routingKey;
	}

	/**
	 * @return the destExchange
	 */
	Exchange destExchange() {
		return destExchange;
	}

	/**
	 * @return the destQueue
	 */
	Queue destQueue() {
		return destQueue;
	}

	/**
	 * @return the destinationType
	 */
	DestType destType() {
		return destType;
	}

	public static void main(String[] args) {

		Exchange exchange0 = Exchange.direct("ABC");
		Exchange exchange1 = Exchange.direct("ABC");
		System.out.println(exchange0);
		System.out.println(exchange1);
		System.out.println(exchange0 == exchange1);
		System.out.println(exchange0.idempotent(exchange1));

		System.out.println(Exchange.direct("ABC"));
		System.out.println(Queue.named("ABC"));

	}

}
