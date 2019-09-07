package io.ffreedom.transport.rabbitmq.declare;

import java.util.Arrays;
import java.util.List;

import javax.annotation.Nonnull;

import io.ffreedom.transport.rabbitmq.config.ConnectionConfigurator;
import io.ffreedom.transport.rabbitmq.declare.BaseEntity.Binding;
import io.ffreedom.transport.rabbitmq.declare.BaseEntity.Exchange;
import io.ffreedom.transport.rabbitmq.declare.BaseEntity.Queue;

public class QueueDeclare extends BaseDeclare {

	private Queue queue;

	public static QueueDeclare declare(String queueName) {
		return new QueueDeclare(Queue.declare(queueName));
	}

	private QueueDeclare(Queue queue) {
		this.queue = queue;
	}

	@Override
	public void declare(ConnectionConfigurator configurator) {

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