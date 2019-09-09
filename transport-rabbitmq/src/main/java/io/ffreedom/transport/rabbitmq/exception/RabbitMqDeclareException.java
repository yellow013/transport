package io.ffreedom.transport.rabbitmq.exception;

import com.rabbitmq.client.BuiltinExchangeType;

public class ExchangeDeclareException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7352101640279556300L;

	public ExchangeDeclareException(String exchange, BuiltinExchangeType type, boolean durable, boolean autoDelete,
			boolean internal) {
		super("Declare exchange error -> exchange == " + exchange + ", type == " + type + ", durable == " + durable
				+ ", autoDelete == " + autoDelete + ", internal == " + internal);
	}

}
