package io.mercury.transport.jeromq;

import java.io.Closeable;
import java.io.IOException;
import java.util.function.Function;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import io.mercury.common.thread.ThreadUtil;
import io.mercury.transport.core.api.Receiver;
import io.mercury.transport.jeromq.config.JeroMqConfigurator;

public class JeroMqPipeline implements Receiver, Closeable {

	private ZContext zCtx;
	private ZMQ.Socket zSocket;

	private String receiverName;

	private Function<byte[], byte[]> pipeline;
	private JeroMqConfigurator configurator;

	private volatile boolean isRun = true;

	public JeroMqPipeline(JeroMqConfigurator configurator, Function<byte[], byte[]> pipeline) {
		if (configurator == null || pipeline == null)
			throw new IllegalArgumentException("configurator is null in JeroMQReceiver init mothed !");
		this.configurator = configurator;
		this.pipeline = pipeline;
		init();
	}

	private void init() {
		this.zCtx = new ZContext(configurator.ioThreads());
		this.zSocket = zCtx.createSocket(SocketType.REP);
		this.zSocket.bind(configurator.host());
		this.receiverName = "JeroMQ.REP:" + configurator.host();
	}

	@Override
	public void receive() {
		while (isRun) {
			byte[] recvBytes = zSocket.recv();
			byte[] sendBytes = pipeline.apply(recvBytes);
			if (sendBytes != null)
				zSocket.send(sendBytes);
		}
		return;
	}

	@Override
	public boolean destroy() {
		this.isRun = false;
		ThreadUtil.sleep(50);
		zSocket.close();
		zCtx.close();
		return zCtx.isClosed();
	}

	@Override
	public String name() {
		return receiverName;
	}

	public static void main(String[] args) {

		try (JeroMqPipeline receiver = new JeroMqPipeline(
				JeroMqConfigurator.builder().ioThreads(10).host("tcp://*:5551").build(), (byte[] byteMsg) -> {
					System.out.println(new String(byteMsg));
					return null;
				})) {
			ThreadUtil.sleep(15000);
			ThreadUtil.startNewThread(() -> receiver.receive());
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	@Override
	public boolean isConnected() {
		return !zCtx.isClosed();
	}

	@Override
	public void reconnect() {
		// TODO Auto-generated method stub

	}

	@Override
	public void close() throws IOException {
		destroy();
	}

}
