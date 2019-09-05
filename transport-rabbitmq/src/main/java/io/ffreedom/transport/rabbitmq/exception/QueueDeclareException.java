package io.ffreedom.transport.rabbitmq.exception;

public class QueueDeclareException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7352101640279556300L;

	public QueueDeclareException(String queue, boolean durable, boolean exclusive, boolean autoDelete) {
		super("Declare queue error -> queue == " + queue + ", durable == " + durable + ", exclusive == " + exclusive
				+ ", autoDelete == " + autoDelete);
	}

}
