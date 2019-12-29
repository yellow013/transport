package io.mercury.transport.jeromq;

import java.io.Closeable;
import java.io.IOException;

import javax.annotation.concurrent.NotThreadSafe;

import org.zeromq.SocketType;
import org.zeromq.ZMQ;

import io.mercury.transport.core.api.Sender;
import io.mercury.transport.jeromq.config.JeroMqConfigurator;

@NotThreadSafe
public class JeroMqSender implements Sender<byte[]>, Closeable {

	private ZMQ.Context context;
	private ZMQ.Socket socket;

	private String senderName;

	private JeroMqConfigurator configurator;

	public JeroMqSender(JeroMqConfigurator configurator) {
		if (configurator == null)
			throw new IllegalArgumentException("configurator is null in JeroMQPublisher init mothed !");
		this.configurator = configurator;
		init();
	}

	private void init() {
		this.context = ZMQ.context(configurator.ioThreads());
		this.socket = context.socket(SocketType.REQ);
		this.socket.connect(configurator.host());
		this.senderName = "JeroMQ.REQ$" + configurator.host();
	}

	@Override
	public void send(byte[] msg) {
		socket.send(msg);
		socket.recv();
	}

	@Override
	public boolean destroy() {
		socket.close();
		context.term();
		return true;
	}

	@Override
	public String name() {
		return senderName;
	}

	public static void main(String[] args) {

		JeroMqConfigurator configurator = JeroMqConfigurator.builder().ioThreads(1).host("tcp://localhost:5551")
				.build();

		try (JeroMqSender sender = new JeroMqSender(configurator)) {

			sender.send("TEST MSG".getBytes());

			sender.destroy();

		} catch (IOException e) {
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