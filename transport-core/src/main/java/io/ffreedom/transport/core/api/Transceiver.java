package io.ffreedom.transport.core.api;

public interface Transceiver<T> extends Receiver {

	Sender<T> getInnerSender();

	void startSend();

}
