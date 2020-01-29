package io.mercury.transport.rabbitmq.declare;

import org.eclipse.collections.api.list.MutableList;
import org.slf4j.Logger;

import io.mercury.common.annotation.lang.ProtectedAbstractMethod;
import io.mercury.common.collections.MutableLists;
import io.mercury.common.log.CommonLoggerFactory;
import io.mercury.transport.rabbitmq.OperationalChannel;
import io.mercury.transport.rabbitmq.declare.entity.Binding;
import io.mercury.transport.rabbitmq.declare.entity.Exchange;
import io.mercury.transport.rabbitmq.declare.entity.Queue;
import io.mercury.transport.rabbitmq.exception.AmqpDeclareException;

public abstract class Relationship {

	protected Logger logger = CommonLoggerFactory.getLogger(getClass());

	protected MutableList<Binding> bindings = MutableLists.newFastList();

	public void declare(OperationalChannel channel) throws AmqpDeclareException {
		declare0(channel);
		for (Binding binding : bindings)
			handleBinding(channel, binding);
	}

	private void handleBinding(OperationalChannel channel, Binding binding) throws AmqpDeclareException {
		Exchange source = binding.source();
		try {
			channel.declareExchange(source);
		} catch (AmqpDeclareException declareException) {
			logger.error("Declare source exchange failure -> {}", source);
			throw declareException;
		}
		String routingKey = binding.routingKey();
		switch (binding.destType()) {
		case Exchange:
			Exchange destExchange = binding.destExchange();
			try {
				channel.declareExchange(destExchange);
			} catch (AmqpDeclareException e) {
				logger.error("Declare dest exchange failure -> {}", destExchange);
				throw e;
			}
			try {
				channel.bindExchange(destExchange.name(), source.name(), routingKey);
			} catch (AmqpDeclareException e) {
				logger.error("Declare bind exchange failure -> dest==[{}], source==[{}], routingKey==[{}]",
						destExchange, source, routingKey);
				throw e;
			}
			break;
		case Queue:
			Queue destQueue = binding.destQueue();
			try {
				channel.declareQueue(destQueue);
			} catch (AmqpDeclareException e) {
				logger.error("Declare dest queue failure -> {}", destQueue);
				throw e;
			}
			try {
				channel.bindQueue(destQueue.name(), source.name(), routingKey);
			} catch (AmqpDeclareException e) {
				logger.error("Declare bind queue failure -> dest==[{}], source==[{}], routingKey==[{}]", destQueue,
						source, routingKey);
				throw e;
			}
			break;
		default:
			break;
		}
	}

	@ProtectedAbstractMethod
	protected abstract void declare0(OperationalChannel opChannel);

}
