package io.ffreedom.transport.rabbitmq.declare;

import java.util.Arrays;
import java.util.List;

import javax.annotation.Nonnull;

import io.ffreedom.transport.rabbitmq.OperationalChannel;
import io.ffreedom.transport.rabbitmq.declare.BaseEntity.Binding;
import io.ffreedom.transport.rabbitmq.declare.BaseEntity.Exchange;
import io.ffreedom.transport.rabbitmq.declare.BaseEntity.Queue;
import io.ffreedom.transport.rabbitmq.exception.RabbitMqDeclareException;

public class QueueDeclare extends BaseDeclare {

	private Queue queue;

	public static QueueDeclare queue(String queueName) {
		return new QueueDeclare(Queue.declare(queueName));
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
	public Queue getQueue() {
		return queue;
	}

	public QueueDeclare setDurable(boolean durable) {
		queue.setDurable(durable);
		return this;
	}

	public QueueDeclare setAutoDelete(boolean autoDelete) {
		queue.setAutoDelete(autoDelete);
		return this;
	}

	public QueueDeclare setExclusive(boolean exclusive) {
		queue.setExclusive(exclusive);
		return this;
	}

	public QueueDeclare declareBinding(Exchange... exchanges) {
		return declareBinding(exchanges != null ? Arrays.asList(exchanges) : null, null);
	}

	public QueueDeclare declareBinding(@Nonnull List<Exchange> exchanges, List<String> routingKeys) {
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