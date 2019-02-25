package io.ffreedom.transport.core.role;

import io.ffreedom.common.queue.api.SCQueue;

public abstract class BaseTransceiver<T> implements Transceiver<T> {

	private InnerSender<T> innerSender;

	private SCQueue<T> queue;

	protected BaseTransceiver() {
		this.queue = initSendQueue();
		this.innerSender = new InnerSenderDevice(queue);
	}

	private class InnerSenderDevice implements InnerSender<T> {

		private SCQueue<T> queue;

		private InnerSenderDevice(SCQueue<T> queue) {
			this.queue = queue;
		}

		@Override
		public void send(T msg) {
			queue.enqueue(msg);
		}

	}

	@Override
	public InnerSender<T> getInnerSender() {
		return innerSender;
	}

	@Override
	public void startSend() {
		queue.start();
	}

	protected abstract SCQueue<T> initSendQueue();

}
