package io.mercury.transport.rabbitmq.declare;

import java.util.Arrays;
import java.util.List;

import javax.annotation.Nonnull;

import io.mercury.transport.rabbitmq.OperationalChannel;
import io.mercury.transport.rabbitmq.declare.EntityDeclare.Binding;
import io.mercury.transport.rabbitmq.declare.EntityDeclare.Exchange;
import io.mercury.transport.rabbitmq.declare.EntityDeclare.Queue;
import io.mercury.transport.rabbitmq.exception.RabbitMqDeclareException;

/**
 * 定义Queue和其他实体绑定关系
 * 
 * @author yellow013
 *
 */
public class QueueDeclare extends Relationship {

	private Queue queue;

	public static QueueDeclare named(String queueName) {
		return new QueueDeclare(Queue.declare(queueName));
	}

	public static QueueDeclare with(Queue queue) {
		return new QueueDeclare(queue);
	}

	private QueueDeclare(Queue queue) {
		this.queue = queue;
	}

	@Override
	protected void declare0(OperationalChannel channel) {
		try {
			channel.declareQueue(queue);
		} catch (RabbitMqDeclareException e) {
			logger.error("Declare Queue failure -> {}", queue);
			throw new RuntimeException(e);
		}
	}

	/**
	 * @return the queue
	 */
	public Queue queue() {
		return queue;
	}

	public QueueDeclare durable(boolean durable) {
		queue.durable(durable);
		return this;
	}

	public QueueDeclare autoDelete(boolean autoDelete) {
		queue.autoDelete(autoDelete);
		return this;
	}

	public QueueDeclare exclusive(boolean exclusive) {
		queue.exclusive(exclusive);
		return this;
	}

	public QueueDeclare binding(Exchange... exchanges) {
		return binding(exchanges != null ? Arrays.asList(exchanges) : null, null);
	}

	public QueueDeclare binding(@Nonnull List<Exchange> exchanges, List<String> routingKeys) {
		if (exchanges != null) {
			exchanges.forEach(exchange -> {
				if (routingKeys != null)
					routingKeys.forEach(routingKey -> bindings.add(new Binding(exchange, this.queue, routingKey)));
				else
					bindings.add(new Binding(exchange, this.queue));
			});
		}
		return this;
	}

	public static void main(String[] args) {

	}

}