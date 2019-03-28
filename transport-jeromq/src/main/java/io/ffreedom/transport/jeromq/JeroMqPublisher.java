package io.ffreedom.transport.jeromq;

import java.util.Random;

import org.zeromq.SocketType;
import org.zeromq.ZMQ;

import io.ffreedom.common.utils.ThreadUtil;
import io.ffreedom.transport.core.role.Publisher;
import io.ffreedom.transport.jeromq.config.JeroMqConfigurator;

public class JeroMqPublisher implements Publisher<byte[]> {

	private ZMQ.Context context;
	private ZMQ.Socket publisher;

	private String topic;

	private String publisherName;

	private JeroMqConfigurator configurator;

	public JeroMqPublisher(JeroMqConfigurator configurator) {
		if (configurator == null)
			throw new IllegalArgumentException("configurator is null in JeroMQPublisher init method.");
		this.configurator = configurator;
		init();
	}

	private void init() {
		this.context = ZMQ.context(configurator.getIoThreads());
		this.publisher = context.socket(SocketType.PUB);
		this.publisher.bind(configurator.getHost());
		this.topic = configurator.getTopic();
		this.publisherName = "JeroMQ.Pub$" + configurator.getHost();
	}

	@Override
	public void publish(byte[] msg) {
		publish(topic, msg);
	}

	@Override
	public void publish(String target, byte[] msg) {
		publisher.sendMore(target);
		publisher.send(msg, ZMQ.NOBLOCK);
	}

	@Override
	public boolean destroy() {
		publisher.close();
		context.term();
		context.close();
		return context.isClosed();
	}

	@Override
	public String getName() {
		return publisherName;
	}

	public static void main(String[] args) {
		JeroMqConfigurator configurator = JeroMqConfigurator.builder().setHost("tcp://*:5559").setIoThreads(1)
				.setTopic("").build();

		JeroMqPublisher publisher = new JeroMqPublisher(configurator);

		Random random = new Random();

		for (;;) {
			publisher.publish(String.valueOf(random.nextInt()).getBytes());
			ThreadUtil.sleep(1000);
		}
	}

	@Override
	public boolean isConnected() {
		return !context.isClosed();
	}

}
