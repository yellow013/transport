package io.ffreedom.transport.rabbitmq.exception;

public class NoConfirmException extends Exception {

	/**
	 *
	 */
	private static final long serialVersionUID = -197190157920481972L;

	public NoConfirmException(String exchange, String routingKey, int confirmRetry, long confirmTimeout, byte[] msg) {
		super("Call confirmPublish failure -> exchange==[" + exchange + "], routingKey==[" + routingKey
				+ "], confirmRetry==[" + confirmRetry + "], confirmTimeout==[" + confirmTimeout + "], msg==["
				+ new String(msg) + "]");
	}

	public NoConfirmException(String exchange, String routingKey, int confirmRetry, long confirmTimeout) {
		super("Call confirmPublish failure -> exchange==[" + exchange + "], routingKey==[" + routingKey
				+ "], confirmRetry==[" + confirmRetry + "], confirmTimeout==[" + confirmTimeout + "]");
	}

}
