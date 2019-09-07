package io.ffreedom.transport.rabbitmq.declare;

import java.util.Arrays;
import java.util.List;

import javax.annotation.Nonnull;

import io.ffreedom.transport.rabbitmq.config.ConnectionConfigurator;
import io.ffreedom.transport.rabbitmq.declare.BaseEntity.Binding;
import io.ffreedom.transport.rabbitmq.declare.BaseEntity.Exchange;
import io.ffreedom.transport.rabbitmq.declare.BaseEntity.Queue;

public class ExchangeDeclare extends BaseDeclare {

	private Exchange exchange;

	public static ExchangeDeclare declareFanoutExchange(@Nonnull String exchangeName) {
		return new ExchangeDeclare(Exchange.declareFanout(exchangeName));
	}

	public static ExchangeDeclare declareDirectExchange(@Nonnull String exchangeName) {
		return new ExchangeDeclare(Exchange.declareDirect(exchangeName));
	}

	public static ExchangeDeclare declareTopicExchange(@Nonnull String exchangeName) {
		return new ExchangeDeclare(Exchange.declareTopic(exchangeName));
	}

	private ExchangeDeclare(Exchange exchange) {
		this.exchange = exchange;
	}

	@Override
	public void declare(ConnectionConfigurator configurator) {
		// TODO Auto-generated method stub
	}

	public ExchangeDeclare setDurable(boolean durable) {
		exchange.setDurable(durable);
		return this;
	}

	public ExchangeDeclare setAutoDelete(boolean autoDelete) {
		exchange.setAutoDelete(autoDelete);
		return this;
	}

	public ExchangeDeclare setInternal(boolean internal) {
		exchange.setInternal(internal);
		return this;
	}

	public ExchangeDeclare declareBindingExchange(Exchange... exchanges) {
		return declareBindingExchange(exchanges != null ? Arrays.asList(exchanges) : null, null);
	}

	public ExchangeDeclare declareBindingExchange(List<Exchange> exchanges, List<String> routingKeys) {
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

	public ExchangeDeclare declareBindingQueue(Queue... queues) {
		return declareBindingQueue(queues != null ? Arrays.asList(queues) : null, null);
	}

	public ExchangeDeclare declareBindingQueue(List<Queue> queues, List<String> routingKeys) {
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

		ExchangeDeclare.declareDirectExchange("TEST_DIRECT").setAutoDelete(true).setInternal(true);

	}

}