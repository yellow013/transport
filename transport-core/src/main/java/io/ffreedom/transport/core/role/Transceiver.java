package io.ffreedom.transport.core.role;

public interface Transceiver<T> extends Receiver {

	Sender<T> getInnerSender();

	void startSend();

}
