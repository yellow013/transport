package io.mercury.transport.rabbitmq.declare;

import org.eclipse.collections.api.list.MutableList;
import org.slf4j.Logger;

import io.mercury.common.annotation.lang.ProtectedAbstractMethod;
import io.mercury.common.collections.MutableLists;
import io.mercury.common.log.CommonLoggerFactory;
import io.mercury.transport.rabbitmq.RabbitMqDeclarant;
import io.mercury.transport.rabbitmq.declare.Binding;
import io.mercury.transport.rabbitmq.declare.Exchange;
import io.mercury.transport.rabbitmq.declare.Queue;
import io.mercury.transport.rabbitmq.exception.AmqpDeclareException;

abstract class Relation {

	protected Logger logger = CommonLoggerFactory.getLogger(getClass());

	protected MutableList<Binding> bindings = MutableLists.newFastList();

	public void declare(RabbitMqDeclarant declarant) throws AmqpDeclareException {
		declare0(declarant);
		for (Binding binding : bindings)
			declareBinding(declarant, binding);
	}

	private void declareBinding(RabbitMqDeclarant declarant, Binding binding) throws AmqpDeclareException {
		Exchange source = binding.source();
		try {
			declarant.declareExchange(source);
		} catch (AmqpDeclareException declareException) {
			logger.error("Declare source exchange failure -> {}", source);
			throw declareException;
		}
		String routingKey = binding.routingKey();
		switch (binding.destType()) {
		case Exchange:
			Exchange destExchange = binding.destExchange();
			try {
				declarant.declareExchange(destExchange);
			} catch (AmqpDeclareException exception) {
				logger.error("Declare dest exchange failure -> {}", destExchange);
				throw exception;
			}
			try {
				declarant.bindExchange(destExchange.name(), source.name(), routingKey);
			} catch (AmqpDeclareException exception) {
				logger.error("Declare bind exchange failure -> dest==[{}], source==[{}], routingKey==[{}]",
						destExchange, source, routingKey);
				throw exception;
			}
			break;
		case Queue:
			Queue destQueue = binding.destQueue();
			try {
				declarant.declareQueue(destQueue);
			} catch (AmqpDeclareException exception) {
				logger.error("Declare dest queue failure -> {}", destQueue);
				throw exception;
			}
			try {
				declarant.bindQueue(destQueue.name(), source.name(), routingKey);
			} catch (AmqpDeclareException exception) {
				logger.error("Declare bind queue failure -> dest==[{}], source==[{}], routingKey==[{}]", destQueue,
						source, routingKey);
				throw exception;
			}
			break;
		default:
			break;
		}
	}

	@ProtectedAbstractMethod
	protected abstract void declare0(RabbitMqDeclarant operator);

}
