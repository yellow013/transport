package io.mercury.transport.rabbitmq;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import javax.annotation.Nonnull;

import com.rabbitmq.client.AMQP.Queue.DeleteOk;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;

import io.mercury.transport.rabbitmq.configurator.RmqConnection;
import io.mercury.transport.rabbitmq.declare.entity.Exchange;
import io.mercury.transport.rabbitmq.declare.entity.Queue;
import io.mercury.transport.rabbitmq.exception.AmqpDeclareException;

public final class OperationalChannel extends AbstractRabbitMqTransport {

	/**
	 * Create OperationalChannel of host, port, username and password
	 * 
	 * @param host
	 * @param port
	 * @param username
	 * @param password
	 * @return
	 * @throws IOException
	 * @throws TimeoutException
	 */
	public static OperationalChannel createChannel(String host, int port, String username, String password)
			throws IOException, TimeoutException {
		return createChannel(RmqConnection.configuration(host, port, username, password).build());
	}

	/**
	 * Create OperationalChannel of host, port, username, password and virtualHost
	 * 
	 * @param host
	 * @param port
	 * @param username
	 * @param password
	 * @param virtualHost
	 * @return
	 * @throws IOException
	 * @throws TimeoutException
	 */
	public static OperationalChannel createChannel(String host, int port, String username, String password,
			String virtualHost) throws IOException, TimeoutException {
		return createChannel(RmqConnection.configuration(host, port, username, password, virtualHost).build());
	}

	/**
	 * Create OperationalChannel of RmqConnection
	 * 
	 * @param configurator
	 * @return
	 * @throws IOException
	 * @throws TimeoutException
	 */
	public static OperationalChannel createChannel(RmqConnection connection) throws IOException, TimeoutException {
		return new OperationalChannel("OperationalChannel", connection);
	}

	/**
	 * Create OperationalChannel of Channel
	 * 
	 * @param channel
	 * @return
	 */
	static OperationalChannel ofChannel(Channel channel) {
		return new OperationalChannel(channel);
	}

	private OperationalChannel(String tag, RmqConnection connection) throws IOException, TimeoutException {
		super(tag, "OperationalChannel", connection);
		createConnection();
	}

	private OperationalChannel(Channel channel) {
		this.channel = channel;
	}

	/**
	 * 
	 * @param String           -> queue name
	 * @param DefaultParameter -> durable == true, exclusive == false, autoDelete ==
	 *                         false<br>
	 * @throws QueueDeclareException
	 */
	public boolean declareQueueDefault(@Nonnull String queue) throws AmqpDeclareException {
		if (queue == null)
			throw AmqpDeclareException.with(new NullPointerException("param queue is can't null."));
		return declareQueue(queue, true, false, false);
	}

	public boolean declareQueue(@Nonnull Queue queue) throws AmqpDeclareException {
		if (queue == null)
			throw AmqpDeclareException.with(new NullPointerException("param queue is can't null."));
		return declareQueue(queue.name(), queue.durable(), queue.exclusive(), queue.autoDelete());
	}

	public boolean declareQueue(@Nonnull String queue, boolean durable, boolean exclusive, boolean autoDelete)
			throws AmqpDeclareException {
		try {
			channel.queueDeclare(queue, durable, exclusive, autoDelete, null);
			return true;
		} catch (Exception e) {
			throw AmqpDeclareException.declareQueueError(queue, durable, exclusive, autoDelete, e);
		}
	}

	/**
	 * 
	 * @param exchange
	 * @param DefaultParameter -> durable == true, autoDelete == false, internal ==
	 *                         false
	 * @return
	 * @throws ExchangeDeclareException
	 */
	public boolean declareExchange(@Nonnull Exchange exchange) throws AmqpDeclareException {
		if (exchange == null)
			throw AmqpDeclareException.with(new NullPointerException("param exchange is can't null."));
		switch (exchange.type()) {
		case Direct:
			return declareDirectExchange(exchange.name(), exchange.durable(), exchange.autoDelete(),
					exchange.internal());
		case Fanout:
			return declareFanoutExchange(exchange.name(), exchange.durable(), exchange.autoDelete(),
					exchange.internal());
		case Topic:
			return declareTopicExchange(exchange.name(), exchange.durable(), exchange.autoDelete(),
					exchange.internal());
		default:
			return false;
		}
	}

	public boolean declareDirectExchangeDefault(@Nonnull String exchange) throws AmqpDeclareException {
		if (exchange == null)
			throw AmqpDeclareException.with(new NullPointerException("param exchange is can't null."));
		return declareDirectExchange(exchange, true, false, false);
	}

	public boolean declareDirectExchange(@Nonnull String exchange, boolean durable, boolean autoDelete,
			boolean internal) throws AmqpDeclareException {
		if (exchange == null)
			throw AmqpDeclareException.with(new NullPointerException("param exchange is can't null."));
		return declareExchange(exchange, BuiltinExchangeType.DIRECT, durable, autoDelete, internal);
	}

	public boolean declareFanoutExchangeDefault(@Nonnull String exchange) throws AmqpDeclareException {
		if (exchange == null)
			throw AmqpDeclareException.with(new NullPointerException("param exchange is can't null."));
		return declareFanoutExchange(exchange, true, false, false);
	}

	public boolean declareFanoutExchange(@Nonnull String exchange, boolean durable, boolean autoDelete,
			boolean internal) throws AmqpDeclareException {
		if (exchange == null)
			throw AmqpDeclareException.with(new NullPointerException("param exchange is can't null."));
		return declareExchange(exchange, BuiltinExchangeType.FANOUT, durable, autoDelete, internal);
	}

	public boolean declareTopicExchangeDefault(@Nonnull String exchange) throws AmqpDeclareException {
		if (exchange == null)
			throw AmqpDeclareException.with(new NullPointerException("param exchange is can't null."));
		return declareTopicExchange(exchange, true, false, false);
	}

	public boolean declareTopicExchange(@Nonnull String exchange, boolean durable, boolean autoDelete, boolean internal)
			throws AmqpDeclareException {
		if (exchange == null)
			throw AmqpDeclareException.with(new NullPointerException("param exchange is can't null."));
		return declareExchange(exchange, BuiltinExchangeType.TOPIC, durable, autoDelete, internal);
	}

	private boolean declareExchange(String exchange, BuiltinExchangeType type, boolean durable, boolean autoDelete,
			boolean internal) throws AmqpDeclareException {
		try {
			channel.exchangeDeclare(exchange, type, durable, autoDelete, internal, null);
			return true;
		} catch (IOException e) {
			throw AmqpDeclareException.declareExchangeError(exchange, type, durable, autoDelete, internal, e);
		}
	}

	public boolean bindQueue(String queue, String exchange) throws AmqpDeclareException {
		return bindQueue(queue, exchange, "");
	}

	public boolean bindQueue(String queue, String exchange, String routingKey) throws AmqpDeclareException {
		try {
			channel.queueBind(queue, exchange, routingKey == null ? "" : routingKey);
			return true;
		} catch (IOException e) {
			throw AmqpDeclareException.bindQueueError(queue, exchange, routingKey, e);
		}
	}

	public boolean bindExchange(String destExchange, String sourceExchange) throws AmqpDeclareException {
		return bindExchange(destExchange, sourceExchange, "");
	}

	public boolean bindExchange(String destExchange, String sourceExchange, String routingKey)
			throws AmqpDeclareException {
		try {
			channel.exchangeBind(destExchange, sourceExchange, routingKey == null ? "" : routingKey);
			return true;
		} catch (IOException e) {
			throw AmqpDeclareException.bindExchangeError(destExchange, sourceExchange, routingKey, e);
		}
	}

	public boolean deleteQueue(String queue, boolean force) throws IOException {
		DeleteOk queueDelete = channel.queueDelete(queue, !force, !force);
		queueDelete.getMessageCount();
		return true;
	}

	public boolean deleteExchange(String exchange, boolean force) throws IOException {
		channel.exchangeDelete(exchange, !force);
		return true;
	}

	public boolean isOpen() {
		return channel.isOpen();
	}

	public boolean close() throws IOException, TimeoutException {
		channel.close();
		connection.close();
		return true;
	}

	@Override
	public String name() {
		return tag;
	}

	@Override
	public boolean destroy() {
		return false;
	}

	public static void main(String[] args) {

		OperationalChannel manualCloseChannel;
		try {
			manualCloseChannel = createChannel("127.0.0.1", 5672, "guest", "guest");
			System.out.println(manualCloseChannel.isOpen());
			try {
				manualCloseChannel.declareFanoutExchange("MarketData", true, false, false);
			} catch (AmqpDeclareException e) {
				e.printStackTrace();
			}
			manualCloseChannel.close();
			System.out.println(manualCloseChannel.isOpen());
		} catch (IOException | TimeoutException e) {

		}
	}

}
