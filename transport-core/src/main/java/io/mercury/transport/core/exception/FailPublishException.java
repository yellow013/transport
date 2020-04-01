package io.mercury.transport.core.exception;

public class FailPublishException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4306166538549739230L;

	public FailPublishException(String message) {
		super(message);
	}

	public FailPublishException(Throwable throwable) {
		super(throwable);
	}

	public FailPublishException(String message, Throwable throwable) {
		super(message, throwable);
	}

}
