package io.mercury.transport.rabbitmq.declare;

import org.eclipse.collections.api.list.MutableList;
import org.slf4j.Logger;

import io.mercury.common.annotation.lang.ProtectedAbstractMethod;
import io.mercury.common.collections.MutableLists;
import io.mercury.common.log.CommonLoggerFactory;
import io.mercury.transport.rabbitmq.DeclareOperator;
import io.mercury.transport.rabbitmq.declare.Binding;
import io.mercury.transport.rabbitmq.declare.Exchange;
import io.mercury.transport.rabbitmq.declare.Queue;
import io.mercury.transport.rabbitmq.exception.AmqpDeclareException;

public abstract class Relation {

	protected Logger logger = CommonLoggerFactory.getLogger(getClass());

	protected MutableList<Binding> bindings = MutableLists.newFastList();

	public void declare(DeclareOperator operator) throws AmqpDeclareException {
		declare0(operator);
		for (Binding binding : bindings)
			declareBinding(operator, binding);
	}

	private void declareBinding(DeclareOperator operator, Binding binding) throws AmqpDeclareException {
		Exchange source = binding.source();
		try {
			operator.declareExchange(source);
		} catch (AmqpDeclareException declareException) {
			logger.error("Declare source exchange failure -> {}", source);
			throw declareException;
		}
		String routingKey = binding.routingKey();
		switch (binding.destType()) {
		case Exchange:
			Exchange destExchange = binding.destExchange();
			try {
				operator.declareExchange(destExchange);
			} catch (AmqpDeclareException exception) {
				logger.error("Declare dest exchange failure -> {}", destExchange);
				throw exception;
			}
			try {
				operator.bindExchange(destExchange.name(), source.name(), routingKey);
			} catch (AmqpDeclareException exception) {
				logger.error("Declare bind exchange failure -> dest==[{}], source==[{}], routingKey==[{}]",
						destExchange, source, routingKey);
				throw exception;
			}
			break;
		case Queue:
			Queue destQueue = binding.destQueue();
			try {
				operator.declareQueue(destQueue);
			} catch (AmqpDeclareException exception) {
				logger.error("Declare dest queue failure -> {}", destQueue);
				throw exception;
			}
			try {
				operator.bindQueue(destQueue.name(), source.name(), routingKey);
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
	protected abstract void declare0(DeclareOperator operator);

}
