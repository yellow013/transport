package io.mercury.transport.jeromq;

import java.io.Closeable;
import java.io.IOException;
import java.util.Random;

import org.zeromq.SocketType;
import org.zeromq.ZMQ;

import io.mercury.common.thread.ThreadUtil;
import io.mercury.transport.core.api.Publisher;
import io.mercury.transport.jeromq.config.JeroMqConfigurator;

public class JeroMqPublisher implements Publisher<byte[]>, Closeable {

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
		this.context = ZMQ.context(configurator.ioThreads());
		this.publisher = context.socket(SocketType.PUB);
		this.publisher.bind(configurator.host());
		this.topic = configurator.topic();
		this.publisherName = "JeroMQ.Pub$" + configurator.host();
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
	public String name() {
		return publisherName;
	}

	public static void main(String[] args) {
//		JeroMqConfigurator configurator = JeroMqConfigurator.builder().setHost("tcp://*:5559").setIoThreads(1)
//				.setTopic("").build();
		
		JeroMqConfigurator configurator = JeroMqConfigurator.builder().host("tcp://127.0.0.1:13001")
				.topic("command").ioThreads(2).build();

		try (JeroMqPublisher publisher = new JeroMqPublisher(configurator)) {
			Random random = new Random();

			for (;;) {
				publisher.publish(String.valueOf(random.nextInt()).getBytes());
				ThreadUtil.sleep(1000);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Override
	public boolean isConnected() {
		return !context.isClosed();
	}

	@Override
	public void close() throws IOException {
		destroy();
	}

}