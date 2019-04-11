package io.ffreedom.transport.jeromq;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import org.zeromq.SocketType;
import org.zeromq.ZMQ;

import io.ffreedom.common.charset.Charsets;
import io.ffreedom.transport.core.role.Subscriber;
import io.ffreedom.transport.jeromq.config.JeroMqConfigurator;

public class JeroMqSubscriber implements Subscriber {

	private ZMQ.Context context;
	private ZMQ.Socket subscriber;

	private String subscriberName;

	private Consumer<byte[]> callback;
	private JeroMqConfigurator configurator;

	private AtomicBoolean isRun = new AtomicBoolean(true);

	public JeroMqSubscriber(JeroMqConfigurator configurator, Consumer<byte[]> callback) {
		if (configurator == null || callback == null)
			throw new IllegalArgumentException("configurator is null in JeroMQSubscriber init mothed !");
		this.configurator = configurator;
		this.callback = callback;
		init();
	}

	private void init() {
		this.context = ZMQ.context(configurator.getIoThreads());
		this.subscriber = context.socket(SocketType.SUB);
		this.subscriber.connect(configurator.getHost());
		this.subscriber.subscribe(configurator.getTopic().getBytes());
		this.subscriber.setTCPKeepAlive(1);
		this.subscriber.setTCPKeepAliveCount(10);
		this.subscriber.setTCPKeepAliveIdle(15);
		this.subscriber.setTCPKeepAliveInterval(15);
		this.subscriberName = "JeroMQ.SUB$" + configurator.getHost() + "::" + configurator.getTopic();
	}

	@Override
	public void subscribe() {
		while (isRun.get()) {
			subscriber.recv();
			byte[] msgBytes = subscriber.recv();
			callback.accept(msgBytes);
		}
	}

	@Override
	public boolean destroy() {
		this.isRun.set(false);
		subscriber.close();
		context.term();
		return true;
	}

	@Override
	public String getName() {
		return subscriberName;
	}

	public static void main(String[] args) {
		JeroMqSubscriber jeroMQSubscriber = new JeroMqSubscriber(
				JeroMqConfigurator.builder().setHost("tcp://127.0.0.1:10001").setIoThreads(2).setTopic("").build(),
				(byte[] byteMsg) -> System.out.println(new String(byteMsg, Charsets.UTF8)));
		jeroMQSubscriber.subscribe();
	}

	@Override
	public boolean isConnected() {
		return false;
	}

}
