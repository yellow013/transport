package io.mercury.transport.rabbitmq;

import java.io.IOException;
import java.util.function.Function;

import io.mercury.codec.json.JsonEncoder;
import io.mercury.common.character.Charsets;
import io.mercury.common.collections.queue.api.Queue;
import io.mercury.transport.rabbitmq.configurator.RmqConnection;
import io.mercury.transport.rabbitmq.exception.AmqpDeclareException;

public class RabbitQueue<E> implements Queue<E> {

	private RmqConnection connection;

	private io.mercury.transport.rabbitmq.declare.entity.Queue queue;

	private String queueName;

	private RabbitMqGeneralChannel generalChannel;

	private Function<E, byte[]> serializer;

	private String name;

	public RabbitQueue(RmqConnection connection, io.mercury.transport.rabbitmq.declare.entity.Queue queue)
			throws AmqpDeclareException {
		this(connection, queue, e -> JsonEncoder.toJson(e).getBytes(Charsets.UTF8));
	}

	public RabbitQueue(RmqConnection connection, io.mercury.transport.rabbitmq.declare.entity.Queue queue,
			Function<E, byte[]> serializer) throws AmqpDeclareException {
		super();
		this.connection = connection;
		this.queue = queue;
		this.serializer = serializer;
		this.queueName = queue.name();
		this.generalChannel = RabbitMqGeneralChannel.create(connection);
		declareQueue();
		buildName();
	}

	private void declareQueue() throws AmqpDeclareException {
		DeclareOperator.ofChannel(generalChannel.getChannel()).declareQueue(queue);
	}

	private void buildName() {
		this.name = "rabbit-queue::" + connection.fullInfo() + "/" + queueName;
	}

	@Override
	public boolean enqueue(E e) {
		try {
			byte[] apply = serializer.apply(e);
			generalChannel.getChannel().basicPublish("", queueName, null, apply);
			return true;
		} catch (IOException e1) {
			return false;
		}
	}

	@Override
	public String name() {
		return name;
	}

}
