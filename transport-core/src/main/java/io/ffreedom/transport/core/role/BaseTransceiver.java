package io.ffreedom.transport.core.role;

import io.ffreedom.common.queue.api.SCQueue;

public abstract class BaseTransceiver<T> implements Transceiver<T> {

	private Sender<T> innerSender;

	private SCQueue<T> queue;

	protected BaseTransceiver() {
		this.queue = initSendQueue();
		this.innerSender = new InnerSender(queue);
	}

	private class InnerSender implements Sender<T> {

		private SCQueue<T> queue;

		private InnerSender(SCQueue<T> queue) {
			this.queue = queue;
		}

		@Override
		public void send(T msg) {
			queue.enqueue(msg);
		}

		@Override
		public String getName() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public boolean isConnected() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean destroy() {
			// TODO Auto-generated method stub
			return false;
		}

	}

	@Override
	public Sender<T> getInnerSender() {
		return innerSender;
	}

	@Override
	public void startSend() {
		queue.start();
	}

	protected abstract SCQueue<T> initSendQueue();

}
