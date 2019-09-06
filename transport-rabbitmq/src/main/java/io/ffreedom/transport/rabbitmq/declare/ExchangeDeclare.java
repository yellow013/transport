package io.ffreedom.transport.rabbitmq.declare;

import javax.annotation.Nonnull;

import org.eclipse.collections.api.list.MutableList;

import io.ffreedom.common.collections.MutableLists;
import io.ffreedom.transport.rabbitmq.config.ConnectionConfigurator;
import io.ffreedom.transport.rabbitmq.declare.BaseEntity.Binding;
import io.ffreedom.transport.rabbitmq.declare.BaseEntity.Exchange;
import io.ffreedom.transport.rabbitmq.declare.BaseEntity.Queue;

public class ExchangeDeclare extends BaseDeclare {

	private Exchange exchange;

	private MutableList<Exchange> bindingExchanges = MutableLists.newFastList();
	private MutableList<Queue> bindingQueues = MutableLists.newFastList();

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

	public ExchangeDeclare bindingExchange(Exchange... exchanges) {
		if (exchanges != null) {
			for (Exchange exchange : exchanges)
				bindingExchanges.add(exchange);
		}
		return this;
	}

	public ExchangeDeclare bindingQueue(Queue... queues) {
		if (queues != null) {
			for (Queue queue : queues)
				bindingQueues.add(queue);
		}
		return this;
	}

	public ExchangeDeclare declareBinding() {
		return declareBinding("");
	}

	public ExchangeDeclare declareBinding(String... routingKeys) {
		bindingQueues.forEach(destination -> {
			if (routingKeys != null) {
				for (String routingKey : routingKeys)
					bindings.add(new Binding(exchange, destination, routingKey));
			} else
				bindings.add(new Binding(exchange, destination));
		});
		bindingQueues.clear();
		bindingExchanges.forEach(destination -> {
			if (routingKeys != null) {
				for (String routingKey : routingKeys)
					bindings.add(new Binding(exchange, destination, routingKey));
			} else
				bindings.add(new Binding(exchange, destination));
		});
		bindingExchanges.clear();
		return this;
	}

	public static void main(String[] args) {

		ExchangeDeclare.declareDirectExchange("TEST_DIRECT").setAutoDelete(true).setInternal(true);

	}

}