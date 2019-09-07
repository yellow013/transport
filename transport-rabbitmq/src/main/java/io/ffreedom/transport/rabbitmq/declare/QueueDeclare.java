package io.ffreedom.transport.rabbitmq.declare;

import org.eclipse.collections.api.list.MutableList;

import io.ffreedom.common.collections.MutableLists;
import io.ffreedom.transport.rabbitmq.config.ConnectionConfigurator;
import io.ffreedom.transport.rabbitmq.declare.BaseEntity.Binding;
import io.ffreedom.transport.rabbitmq.declare.BaseEntity.Exchange;
import io.ffreedom.transport.rabbitmq.declare.BaseEntity.Queue;

public class ReceiverDeclare extends BaseDeclare {

	private Queue queue;
	private ExchangeDeclare errorMsgExchange;
	private MutableList<Exchange> bindingExchanges = MutableLists.newFastList();

	public static ReceiverDeclare declareQueue(String queueName) {
		return new ReceiverDeclare(Queue.declare(queueName));
	}

	private ReceiverDeclare(Queue queue) {
		this.queue = queue;
	}

	@Override
	public void declare(ConnectionConfigurator configurator) {

	}

	public ReceiverDeclare setDurable(boolean durable) {
		queue.setDurable(durable);
		return this;
	}

	public ReceiverDeclare setAutoDelete(boolean autoDelete) {
		queue.setAutoDelete(autoDelete);
		return this;
	}

	public ReceiverDeclare setExclusive(boolean exclusive) {
		queue.setExclusive(exclusive);
		return this;
	}

	public ExchangeDeclare getErrorMsgExchange() {
		return errorMsgExchange;
	}

	public ReceiverDeclare setErrorMsgExchange(ExchangeDeclare errorMsgExchange) {
		this.errorMsgExchange = errorMsgExchange;
		return this;
	}

	public ReceiverDeclare bindingExchange(Exchange... exchanges) {
		if (exchanges != null) {
			for (Exchange exchange : exchanges)
				bindingExchanges.add(exchange);
		}
		return this;
	}

	public ReceiverDeclare declareBinding() {
		return declareBinding("");
	}

	public ReceiverDeclare declareBinding(String... routingKeys) {
		bindingExchanges.forEach(source -> {
			if (routingKeys != null) {
				for (String routingKey : routingKeys)
					bindings.add(new Binding(source, queue, routingKey));
			} else
				bindings.add(new Binding(source, queue));
		});
		bindingExchanges.clear();
		return this;
	}

}