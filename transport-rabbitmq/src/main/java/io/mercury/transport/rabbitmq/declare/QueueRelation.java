package io.mercury.transport.rabbitmq.declare;

import java.util.Arrays;
import java.util.List;

import io.mercury.transport.rabbitmq.DeclareOperator;
import io.mercury.transport.rabbitmq.exception.AmqpDeclareException;

/**
 * 定义Queue和其他实体绑定关系
 * 
 * @author yellow013
 *
 */
public class QueueRelation extends Relation {

	private Queue queue;

	public static QueueRelation named(String queueName) {
		return new QueueRelation(Queue.named(queueName));
	}

	public static QueueRelation with(Queue queue) {
		return new QueueRelation(queue);
	}

	private QueueRelation(Queue queue) {
		this.queue = queue;
	}

	@Override
	protected void declare0(DeclareOperator operator) {
		try {
			operator.declareQueue(queue);
		} catch (AmqpDeclareException e) {
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

	/**
	 * <b>queue().name()<b><br>
	 * 
	 * @return the queue name
	 */
	public String queueName() {
		return queue.name();
	}

	public QueueRelation durable(boolean durable) {
		queue.durable(durable);
		return this;
	}

	public QueueRelation autoDelete(boolean autoDelete) {
		queue.autoDelete(autoDelete);
		return this;
	}

	public QueueRelation exclusive(boolean exclusive) {
		queue.exclusive(exclusive);
		return this;
	}

	public QueueRelation binding(Exchange... exchanges) {
		return binding(exchanges != null ? Arrays.asList(exchanges) : null, null);
	}

	public QueueRelation binding(List<Exchange> exchanges, List<String> routingKeys) {
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