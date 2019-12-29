package io.mercury.transport.jeromq;

import java.util.function.Function;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ.Socket;

import io.mercury.common.thread.ThreadUtil;
import io.mercury.transport.core.api.Receiver;
import io.mercury.transport.jeromq.config.JeroMqConfigurator;

public class JeroMqPipeline implements Receiver {

	private ZContext context;
	private Socket socket;

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
		this.context = new ZContext(configurator.ioThreads());
		this.socket = context.createSocket(SocketType.REP);
		this.socket.bind(configurator.host());
		this.receiverName = "JeroMQ.REP$" + configurator.host();
	}

	@Override
	public void receive() {
		while (isRun) {
			byte[] recvBytes = socket.recv();
			byte[] bytes = pipeline.apply(recvBytes);
			if (bytes == null)
				bytes = new byte[0];
			socket.send(bytes);
		}
		return;
	}

	@Override
	public boolean destroy() {
		this.isRun = false;
		ThreadUtil.sleep(50);
		socket.close();
		context.close();
		return true;
	}

	@Override
	public String name() {
		return receiverName;
	}

	public static void main(String[] args) {

		JeroMqPipeline receiver = new JeroMqPipeline(
				JeroMqConfigurator.builder().ioThreads(10).host("tcp://*:5551").build(), (byte[] byteMsg) -> {
					System.out.println(new String(byteMsg));
					return null;
				});

		ThreadUtil.startNewThread(() -> receiver.receive());

		ThreadUtil.sleep(15000);

		receiver.destroy();

	}

	@Override
	public boolean isConnected() {
		return !context.isClosed();
	}

	@Override
	public void reconnect() {
		// TODO Auto-generated method stub

	}

}