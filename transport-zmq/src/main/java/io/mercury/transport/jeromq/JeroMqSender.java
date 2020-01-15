package io.mercury.transport.jeromq;

import java.io.Closeable;
import java.io.IOException;

import javax.annotation.concurrent.NotThreadSafe;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import io.mercury.transport.core.api.Sender;
import io.mercury.transport.jeromq.config.JeroMqConfigurator;

@NotThreadSafe
public class JeroMqSender implements Sender<byte[]>, Closeable {

	private ZContext zCtx;
	private ZMQ.Socket zSocket;

	private String senderName;

	private JeroMqConfigurator configurator;

	public JeroMqSender(JeroMqConfigurator configurator) {
		if (configurator == null)
			throw new IllegalArgumentException("configurator is null in JeroMQPublisher init mothed !");
		this.configurator = configurator;
		init();
	}

	private void init() {
		this.zCtx = new ZContext(configurator.ioThreads());
		this.zSocket = zCtx.createSocket(SocketType.REQ);
		this.zSocket.connect(configurator.host());
		this.senderName = "JeroMQ.REQ$" + configurator.host();
	}

	@Override
	public void send(byte[] msg) {
		zSocket.send(msg);
		zSocket.recv();
	}

	@Override
	public boolean destroy() {
		zSocket.close();
		zCtx.close();
		return zCtx.isClosed();
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
		return !zCtx.isClosed();
	}

	@Override
	public void close() throws IOException {
		destroy();
	}

}
