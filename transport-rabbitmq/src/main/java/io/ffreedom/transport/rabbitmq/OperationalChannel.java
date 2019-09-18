package io.ffreedom.transport.rabbitmq;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import javax.annotation.Nonnull;

import com.rabbitmq.client.AMQP.Queue.DeleteOk;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;

import io.ffreedom.transport.rabbitmq.config.ConnectionConfigurator;
import io.ffreedom.transport.rabbitmq.declare.BaseEntity.Exchange;
import io.ffreedom.transport.rabbitmq.declare.BaseEntity.Queue;
import io.ffreedom.transport.rabbitmq.exception.RabbitMqDeclareException;

public final class OperationalChannel extends BaseRabbitMqTransport {

	public static OperationalChannel createChannel(String host, int port, String username, String password)
			throws IOException, TimeoutException {
		return createChannel(ConnectionConfigurator.configuration(host, port, username, password).build());
	}

	public static OperationalChannel createChannel(String host, int port, String username, String password,
			String virtualHost) throws IOException, TimeoutException {
		return createChannel(ConnectionConfigurator.configuration(host, port, username, password, virtualHost).build());
	}

	public static OperationalChannel createChannel(ConnectionConfigurator configurator)
			throws IOException, TimeoutException {
		return new OperationalChannel("OperationalChannel", configurator);
	}

	public static OperationalChannel ofChannel(Channel channel) {
		return new OperationalChannel(channel);
	}

	private OperationalChannel(String tag, ConnectionConfigurator configurator) throws IOException, TimeoutException {
		super(tag, tag, configurator);
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
	public boolean declareQueueDefault(@Nonnull String queue) throws RabbitMqDeclareException {
		if (queue == null)
			throw RabbitMqDeclareException.ofException(new NullPointerException("param queue is can't null."));
		return declareQueue(queue, true, false, false);
	}

	public boolean declareQueue(@Nonnull Queue queue) throws RabbitMqDeclareException {
		if (queue == null)
			throw RabbitMqDeclareException.ofException(new NullPointerException("param queue is can't null."));
		return declareQueue(queue.getName(), queue.isDurable(), queue.isExclusive(), queue.isAutoDelete());
	}

	public boolean declareQueue(@Nonnull String queue, boolean durable, boolean exclusive, boolean autoDelete)
			throws RabbitMqDeclareException {
		try {
			channel.queueDeclare(queue, durable, exclusive, autoDelete, null);
			return true;
		} catch (Exception e) {
			throw RabbitMqDeclareException.declareQueueError(queue, durable, exclusive, autoDelete, e);
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
	public boolean declareExchange(@Nonnull Exchange exchange) throws RabbitMqDeclareException {
		if (exchange == null)
			throw RabbitMqDeclareException.ofException(new NullPointerException("param exchange is can't null."));
		switch (exchange.getType()) {
		case Direct:
			return declareDirectExchange(exchange.getName(), exchange.isDurable(), exchange.isAutoDelete(),
					exchange.isInternal());
		case Fanout:
			return declareFanoutExchange(exchange.getName(), exchange.isDurable(), exchange.isAutoDelete(),
					exchange.isInternal());
		case Topic:
			return declareTopicExchange(exchange.getName(), exchange.isDurable(), exchange.isAutoDelete(),
					exchange.isInternal());
		default:
			return false;
		}
	}

	public boolean declareDirectExchangeDefault(@Nonnull String exchange) throws RabbitMqDeclareException {
		if (exchange == null)
			throw RabbitMqDeclareException.ofException(new NullPointerException("param exchange is can't null."));
		return declareDirectExchange(exchange, true, false, false);
	}

	public boolean declareDirectExchange(@Nonnull String exchange, boolean durable, boolean autoDelete,
			boolean internal) throws RabbitMqDeclareException {
		if (exchange == null)
			throw RabbitMqDeclareException.ofException(new NullPointerException("param exchange is can't null."));
		return declareExchange(exchange, BuiltinExchangeType.DIRECT, durable, autoDelete, internal);
	}

	public boolean declareFanoutExchangeDefault(@Nonnull String exchange) throws RabbitMqDeclareException {
		if (exchange == null)
			throw RabbitMqDeclareException.ofException(new NullPointerException("param exchange is can't null."));
		return declareFanoutExchange(exchange, true, false, false);
	}

	public boolean declareFanoutExchange(@Nonnull String exchange, boolean durable, boolean autoDelete,
			boolean internal) throws RabbitMqDeclareException {
		if (exchange == null)
			throw RabbitMqDeclareException.ofException(new NullPointerException("param exchange is can't null."));
		return declareExchange(exchange, BuiltinExchangeType.FANOUT, durable, autoDelete, internal);
	}

	public boolean declareTopicExchangeDefault(@Nonnull String exchange) throws RabbitMqDeclareException {
		if (exchange == null)
			throw RabbitMqDeclareException.ofException(new NullPointerException("param exchange is can't null."));
		return declareTopicExchange(exchange, true, false, false);
	}

	public boolean declareTopicExchange(@Nonnull String exchange, boolean durable, boolean autoDelete, boolean internal)
			throws RabbitMqDeclareException {
		if (exchange == null)
			throw RabbitMqDeclareException.ofException(new NullPointerException("param exchange is can't null."));
		return declareExchange(exchange, BuiltinExchangeType.TOPIC, durable, autoDelete, internal);
	}

	private boolean declareExchange(String exchange, BuiltinExchangeType type, boolean durable, boolean autoDelete,
			boolean internal) throws RabbitMqDeclareException {
		try {
			channel.exchangeDeclare(exchange, type, durable, autoDelete, internal, null);
			return true;
		} catch (IOException e) {
			throw RabbitMqDeclareException.declareExchangeError(exchange, type, durable, autoDelete, internal, e);
		}
	}

	public boolean bindQueue(String queue, String exchange) throws RabbitMqDeclareException {
		return bindQueue(queue, exchange, "");
	}

	public boolean bindQueue(String queue, String exchange, String routingKey) throws RabbitMqDeclareException {
		try {
			channel.queueBind(queue, exchange, routingKey == null ? "" : routingKey);
			return true;
		} catch (IOException e) {
			throw RabbitMqDeclareException.bindQueueError(queue, exchange, routingKey, e);
		}
	}

	public boolean bindExchange(String destExchange, String sourceExchange) throws RabbitMqDeclareException {
		return bindExchange(destExchange, sourceExchange, "");
	}

	public boolean bindExchange(String destExchange, String sourceExchange, String routingKey)
			throws RabbitMqDeclareException {
		try {
			channel.exchangeBind(destExchange, sourceExchange, routingKey == null ? "" : routingKey);
			return true;
		} catch (IOException e) {
			throw RabbitMqDeclareException.bindExchangeError(destExchange, sourceExchange, routingKey, e);
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
	public String getName() {
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
			} catch (RabbitMqDeclareException e) {
				e.printStackTrace();
			}
			manualCloseChannel.close();
			System.out.println(manualCloseChannel.isOpen());
		} catch (IOException | TimeoutException e) {

		}
	}

}
