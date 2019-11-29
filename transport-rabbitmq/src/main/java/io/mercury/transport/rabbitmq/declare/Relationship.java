package io.mercury.transport.rabbitmq.declare;

import org.eclipse.collections.api.list.MutableList;
import org.slf4j.Logger;

import io.mercury.common.collections.MutableLists;
import io.mercury.common.log.CommonLoggerFactory;
import io.mercury.transport.rabbitmq.OperationalChannel;
import io.mercury.transport.rabbitmq.declare.EntityDeclare.Binding;
import io.mercury.transport.rabbitmq.declare.EntityDeclare.Exchange;
import io.mercury.transport.rabbitmq.declare.EntityDeclare.Queue;
import io.mercury.transport.rabbitmq.exception.RabbitMqDeclareException;

public abstract class Relationship {

	protected Logger logger = CommonLoggerFactory.getLogger(getClass());

	protected MutableList<Binding> bindings = MutableLists.newFastList();

	public void declare(OperationalChannel channel) throws RabbitMqDeclareException {
		declare0(channel);
		for (Binding binding : bindings)
			handleBinding(channel, binding);
	}

	private void handleBinding(OperationalChannel channel, Binding binding) throws RabbitMqDeclareException {
		Exchange source = binding.source();
		try {
			channel.declareExchange(source);
		} catch (RabbitMqDeclareException declareException) {
			logger.error("Declare source exchange failure -> {}", source);
			throw declareException;
		}
		String routingKey = binding.routingKey();
		switch (binding.destinationType()) {
		case Exchange:
			Exchange destExchange = binding.destExchange();
			try {
				channel.declareExchange(destExchange);
			} catch (RabbitMqDeclareException declareException) {
				logger.error("Declare dest exchange failure -> {}", destExchange);
				throw declareException;
			}
			try {
				channel.bindExchange(destExchange.name(), source.name(), routingKey);
			} catch (RabbitMqDeclareException declareException) {
				logger.error("Declare bind exchange failure -> dest==[{}], source==[{}], routingKey==[{}]",
						destExchange, source, routingKey);
				throw declareException;
			}
			return;
		case Queue:
			Queue destQueue = binding.destQueue();
			try {
				channel.declareQueue(destQueue);
			} catch (RabbitMqDeclareException declareException) {
				logger.error("Declare dest queue failure -> {}", destQueue);
				throw declareException;
			}
			try {
				channel.bindQueue(destQueue.name(), source.name(), routingKey);
			} catch (RabbitMqDeclareException declareException) {
				logger.error("Declare bind queue failure -> dest==[{}], source==[{}], routingKey==[{}]", destQueue);
				throw declareException;
			}
			return;
		default:
			return;
		}
	}

	protected abstract void declare0(OperationalChannel operationalChannel);

}
