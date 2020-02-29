package io.mercury.transport.rabbitmq.exception;

import com.rabbitmq.client.BuiltinExchangeType;

public final class AmqpDeclareException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7352101640279556300L;

	private AmqpDeclareException(Throwable cause) {
		super(cause);
	}

	private AmqpDeclareException(String message, Throwable cause) {
		super(message, cause);
	}

	public static AmqpDeclareException declareQueueError(String queue, boolean durable, boolean exclusive,
			boolean autoDelete, Throwable cause) {
		return new AmqpDeclareException("Declare queue error -> queue==[" + queue + "], durable==[" + durable
				+ "], exclusive==[" + exclusive + "], autoDelete==[" + autoDelete + "]", cause);
	}

	public static AmqpDeclareException declareExchangeError(String exchange, BuiltinExchangeType type,
			boolean durable, boolean autoDelete, boolean internal, Throwable cause) {
		return new AmqpDeclareException("Declare exchange error -> exchange==[" + exchange + "], type==[" + type
				+ "], durable==[" + durable + "], autoDelete==[" + autoDelete + "], internal==[" + internal + "]",
				cause);
	}

	public static AmqpDeclareException bindQueueError(String queue, String exchange, String routingKey,
			Throwable cause) {
		return new AmqpDeclareException("Declare bind queue error -> queue==[" + queue + "], exchange==[" + exchange
				+ "], routingKey==[" + routingKey + "]", cause);
	}

	public static AmqpDeclareException bindExchangeError(String destExchange, String sourceExchange,
			String routingKey, Throwable cause) {
		return new AmqpDeclareException("Declare bind exchange error -> destExchange==[" + destExchange
				+ "], sourceExchange==[" + sourceExchange + "], routingKey==[" + routingKey + "]", cause);
	}

	public static AmqpDeclareException with(Throwable cause) {
		return new AmqpDeclareException(cause);
	}

}
