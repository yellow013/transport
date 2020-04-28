package io.mercury.transport.rabbitmq.declare;

import java.util.List;

import org.apache.commons.collections4.CollectionUtils;

import io.mercury.common.collections.MutableLists;
import io.mercury.transport.rabbitmq.RabbitMqDeclarant;
import io.mercury.transport.rabbitmq.exception.AmqpDeclareException;

/**
 * 定义Queue和其他实体绑定关系
 * 
 * @author yellow013
 *
 */
public final class QueueAndBinding extends Relation {

	private AmqpQueue queue;

	public static QueueAndBinding named(String queueName) {
		return new QueueAndBinding(AmqpQueue.named(queueName));
	}

	public static QueueAndBinding withQueue(AmqpQueue queue) {
		return new QueueAndBinding(queue);
	}

	private QueueAndBinding(AmqpQueue queue) {
		this.queue = queue;
	}

	@Override
	protected void declare0(RabbitMqDeclarant declarant) {
		try {
			declarant.declareQueue(queue);
		} catch (AmqpDeclareException e) {
			log.error("Declare Queue failure -> {}", queue);
			throw new RuntimeException(e);
		}
	}

	/**
	 * @return the queue
	 */
	public AmqpQueue queue() {
		return queue;
	}

	/**
	 * <b>queue().name()<b><br>
	 * 
	 * @return the queue name
	 */
	public String queueName() {
		return queue.name();
	}

	public QueueAndBinding queueDurable(boolean durable) {
		queue.durable(durable);
		return this;
	}

	public QueueAndBinding queueAutoDelete(boolean autoDelete) {
		queue.autoDelete(autoDelete);
		return this;
	}

	public QueueAndBinding queueExclusive(boolean exclusive) {
		queue.exclusive(exclusive);
		return this;
	}

	public QueueAndBinding binding(AmqpExchange... exchanges) {
		return binding(exchanges != null ? MutableLists.newFastList(exchanges) : null, null);
	}

	public QueueAndBinding binding(List<AmqpExchange> exchanges, List<String> routingKeys) {
		if (exchanges != null) {
			exchanges.forEach(exchange -> {
				if (CollectionUtils.isNotEmpty(routingKeys))
					routingKeys.forEach(routingKey -> bindings.add(new Binding(exchange, queue, routingKey)));
				else
					bindings.add(new Binding(exchange, queue));
			});
		}
		return this;
	}

	public static void main(String[] args) {

	}

}