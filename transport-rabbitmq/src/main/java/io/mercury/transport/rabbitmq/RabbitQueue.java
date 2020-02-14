package io.mercury.transport.rabbitmq;

import java.io.Closeable;
import java.io.IOException;
import java.util.function.Function;

import org.slf4j.Logger;

import com.rabbitmq.client.GetResponse;

import io.mercury.codec.json.JsonUtil;
import io.mercury.common.character.Charsets;
import io.mercury.common.collections.queue.api.Queue;
import io.mercury.common.log.CommonLoggerFactory;
import io.mercury.transport.rabbitmq.configurator.RmqConnection;
import io.mercury.transport.rabbitmq.exception.AmqpDeclareException;

public class RabbitQueue<E> implements Queue<E>, Closeable {

	private RmqConnection connection;
	private RabbitMqGeneralChannel generalChannel;
	private String queueName;

	private Function<E, byte[]> serializer;
	private Function<byte[], E> deserializer;

	private String name;

	private Logger logger = CommonLoggerFactory.getLogger(getClass());

	public static final RabbitQueue<String> newQueue(RmqConnection connection, String queueName)
			throws AmqpDeclareException {
		return new RabbitQueue<>(connection, queueName, e -> JsonUtil.toJson(e).getBytes(Charsets.UTF8),
				bytes -> new String(bytes, Charsets.UTF8));
	}

	public static final <E> RabbitQueue<E> newQueue(RmqConnection connection, String queueName,
			Function<E, byte[]> serializer, Function<byte[], E> deserializer) throws AmqpDeclareException {
		return new RabbitQueue<>(connection, queueName, serializer, deserializer);
	}

	private RabbitQueue(RmqConnection connection, String queueName, Function<E, byte[]> serializer,
			Function<byte[], E> deserializer) throws AmqpDeclareException {
		this.connection = connection;
		this.queueName = queueName;
		this.serializer = serializer;
		this.deserializer = deserializer;
		this.generalChannel = RabbitMqGeneralChannel.create(connection);
		declareQueue();
		buildName();
	}

	private void declareQueue() throws AmqpDeclareException {
		DeclareOperator.ofChannel(generalChannel.getChannel())
				.declareQueue(io.mercury.transport.rabbitmq.declare.Queue.named(queueName));
	}

	private void buildName() {
		this.name = "rabbit-queue::" + connection.fullInfo() + "/" + queueName;
	}

	@Override
	public boolean enqueue(E e) {
		byte[] msg = serializer.apply(e);
		try {
			generalChannel.getChannel().basicPublish("", queueName, null, msg);
			return true;
		} catch (IOException ioe) {
			logger.error("enqueue basicPublish throw -> {}", ioe.getMessage(), ioe);
			return false;
		}
	}

	@Override
	public E poll() {
		GetResponse response = null;
		try {
			response = generalChannel.getChannel().basicGet(queueName, false);
		} catch (IOException ioe) {
			logger.error("poll basicGet throw -> {}", ioe.getMessage(), ioe);
			return null;
		}
		byte[] body = response.getBody();
		if (body != null) {
			try {
				generalChannel.getChannel().basicAck(response.getEnvelope().getDeliveryTag(), true);
			} catch (IOException ioe) {
				logger.error("poll basicAck throw -> {}", ioe.getMessage(), ioe);
				return null;
			}
			return deserializer.apply(body);
		} else
			return null;
	}

	@Override
	public boolean pollAndApply(PollFunction<E> function) {
		GetResponse response = null;
		try {
			response = generalChannel.getChannel().basicGet(queueName, false);
		} catch (IOException ioe) {
			logger.error("poll basicGet throw -> {}", ioe.getMessage(), ioe);
			return false;
		}
		if (response == null)
			return false;
		byte[] body = response.getBody();
		if (body == null) {
			return false;
		}
		E e = deserializer.apply(body);
		boolean apply = function.apply(e);
		if (apply) {
			try {
				generalChannel.getChannel().basicAck(response.getEnvelope().getDeliveryTag(), true);
				return true;
			} catch (IOException ioe) {
				logger.error("poll basicAck throw -> {}", ioe.getMessage(), ioe);
				return false;
			}
		} else {
			logger.error("PollFunction failure, no ack");
			return false;
		}
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public void close() throws IOException {
		generalChannel.close();
	}

}
