package io.ffreedom.transport.rabbitmq.exception;

import com.rabbitmq.client.BuiltinExchangeType;

public class RabbitMqDeclareException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7352101640279556300L;

	private RabbitMqDeclareException(Throwable cause) {
		super(cause);
	}

	private RabbitMqDeclareException(String message, Throwable cause) {
		super(message, cause);
	}

	public static RabbitMqDeclareException declareQueueError(String queue, boolean durable, boolean exclusive,
			boolean autoDelete, Throwable cause) {
		return new RabbitMqDeclareException("Declare queue error -> queue == " + queue + ", durable == " + durable
				+ ", exclusive == " + exclusive + ", autoDelete == " + autoDelete, cause);
	}

	public static RabbitMqDeclareException declareExchangeError(String exchange, BuiltinExchangeType type,
			boolean durable, boolean autoDelete, boolean internal, Throwable cause) {
		return new RabbitMqDeclareException("Declare exchange error -> exchange == " + exchange + ", type == " + type
				+ ", durable == " + durable + ", autoDelete == " + autoDelete + ", internal == " + internal, cause);
	}

	public static RabbitMqDeclareException bindQueueError(String queue, String exchange, String routingKey,
			Throwable cause) {
		return new RabbitMqDeclareException("Declare bind queue error -> queue == " + queue + ", exchange == "
				+ exchange + ", routingKey == " + routingKey, cause);
	}

	public static RabbitMqDeclareException bindExchangeError(String destExchange, String sourceExchange,
			String routingKey, Throwable cause) {
		return new RabbitMqDeclareException("Declare bind exchange error -> destExchange == " + destExchange
				+ ", sourceExchange == " + sourceExchange + ", routingKey == " + routingKey, cause);
	}

	public static RabbitMqDeclareException ofException(Throwable cause) {
		return new RabbitMqDeclareException(cause);
	}

}
