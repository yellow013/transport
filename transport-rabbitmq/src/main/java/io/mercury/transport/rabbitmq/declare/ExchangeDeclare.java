package io.mercury.transport.rabbitmq.declare;

import java.util.Arrays;
import java.util.List;

import javax.annotation.Nonnull;

import io.mercury.transport.rabbitmq.OperationalChannel;
import io.mercury.transport.rabbitmq.declare.AmqpEntity.Binding;
import io.mercury.transport.rabbitmq.declare.AmqpEntity.Exchange;
import io.mercury.transport.rabbitmq.declare.AmqpEntity.Queue;
import io.mercury.transport.rabbitmq.exception.RabbitMqDeclareException;

/**
 * 定义Exchange和其他实体绑定关系
 * 
 * @author yellow013
 *
 */
public class ExchangeDeclare extends Relationship {

	private Exchange exchange;

	public static ExchangeDeclare fanout(@Nonnull String exchangeName) {
		return new ExchangeDeclare(Exchange.fanout(exchangeName));
	}

	public static ExchangeDeclare direct(@Nonnull String exchangeName) {
		return new ExchangeDeclare(Exchange.direct(exchangeName));
	}

	public static ExchangeDeclare topic(@Nonnull String exchangeName) {
		return new ExchangeDeclare(Exchange.topic(exchangeName));
	}

	public static ExchangeDeclare ofExchange(@Nonnull Exchange exchange) {
		return new ExchangeDeclare(exchange);
	}

	private ExchangeDeclare(Exchange exchange) {
		this.exchange = exchange;
	}

	@Override
	protected void declare0(OperationalChannel channel) {
		try {
			channel.declareExchange(exchange);
		} catch (RabbitMqDeclareException e) {
			logger.error("Declare Exchange failure -> {}", exchange);
			throw new RuntimeException(e);
		}
	}

	/**
	 * @return the exchange
	 */
	public Exchange exchange() {
		return exchange;
	}

	public ExchangeDeclare durable(boolean durable) {
		exchange.durable(durable);
		return this;
	}

	public ExchangeDeclare autoDelete(boolean autoDelete) {
		exchange.autoDelete(autoDelete);
		return this;
	}

	public ExchangeDeclare internal(boolean internal) {
		exchange.internal(internal);
		return this;
	}

	public ExchangeDeclare bindingExchange(Exchange... exchanges) {
		return bindingExchange(exchanges != null ? Arrays.asList(exchanges) : null, null);
	}

	public ExchangeDeclare bindingExchange(List<Exchange> exchanges, List<String> routingKeys) {
		if (exchanges != null) {
			exchanges.forEach(exchange -> {
				if (routingKeys != null)
					routingKeys.forEach(routingKey -> bindings.add(new Binding(this.exchange, exchange, routingKey)));
				else
					bindings.add(new Binding(this.exchange, exchange));
			});
		}
		return this;
	}

	public ExchangeDeclare bindingQueue(Queue... queues) {
		return bindingQueue(queues != null ? Arrays.asList(queues) : null, null);
	}

	public ExchangeDeclare bindingQueue(List<Queue> queues, List<String> routingKeys) {
		if (queues != null) {
			queues.forEach(queue -> {
				if (routingKeys != null)
					routingKeys.forEach(routingKey -> bindings.add(new Binding(this.exchange, queue, routingKey)));
				else
					bindings.add(new Binding(this.exchange, queue));
			});
		}
		return this;
	}

	public static void main(String[] args) {

		ExchangeDeclare.direct("TEST_DIRECT").autoDelete(true).internal(true);

	}

}