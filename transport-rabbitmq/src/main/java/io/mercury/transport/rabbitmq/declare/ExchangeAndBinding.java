package io.mercury.transport.rabbitmq.declare;

import java.util.List;

import javax.annotation.Nonnull;

import org.apache.commons.collections4.CollectionUtils;

import io.mercury.common.collections.MutableLists;
import io.mercury.transport.rabbitmq.RabbitMqDeclarant;
import io.mercury.transport.rabbitmq.exception.AmqpDeclareException;

/**
 * 定义Exchange和其他实体绑定关系
 * 
 * @author yellow013
 *
 */
public class ExchangeAndBinding extends Relation {

	public final static ExchangeAndBinding Anonymous = new ExchangeAndBinding(Exchange.Anonymous);

	private Exchange exchange;

	public static ExchangeAndBinding fanout(@Nonnull String exchangeName) {
		return new ExchangeAndBinding(Exchange.fanout(exchangeName));
	}

	public static ExchangeAndBinding direct(@Nonnull String exchangeName) {
		return new ExchangeAndBinding(Exchange.direct(exchangeName));
	}

	public static ExchangeAndBinding topic(@Nonnull String exchangeName) {
		return new ExchangeAndBinding(Exchange.topic(exchangeName));
	}

	public static ExchangeAndBinding withExchange(@Nonnull Exchange exchange) {
		return new ExchangeAndBinding(exchange);
	}

	private ExchangeAndBinding(Exchange exchange) {
		this.exchange = exchange;
	}

	@Override
	protected void declare0(RabbitMqDeclarant declarant) {
		try {
			declarant.declareExchange(exchange);
		} catch (AmqpDeclareException e) {
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

	/**
	 * <b>exchange().name()<b><br>
	 * 
	 * @return the exchange name
	 */
	public String exchangeName() {
		return exchange.name();
	}

	public ExchangeAndBinding exchangeDurable(boolean durable) {
		exchange.durable(durable);
		return this;
	}

	public ExchangeAndBinding exchangeAutoDelete(boolean autoDelete) {
		exchange.autoDelete(autoDelete);
		return this;
	}

	public ExchangeAndBinding exchangeInternal(boolean internal) {
		exchange.internal(internal);
		return this;
	}

	public ExchangeAndBinding bindingExchange(Exchange... exchanges) {
		return bindingExchange(exchanges != null ? MutableLists.newFastList(exchanges) : null, null);
	}

	public ExchangeAndBinding bindingExchange(List<Exchange> exchanges, List<String> routingKeys) {
		if (exchanges != null) {
			exchanges.forEach(exchange -> {
				if (CollectionUtils.isNotEmpty(routingKeys))
					routingKeys.forEach(routingKey -> bindings.add(new Binding(this.exchange, exchange, routingKey)));
				else
					bindings.add(new Binding(this.exchange, exchange));
			});
		}
		return this;
	}

	public ExchangeAndBinding bindingQueue(Queue... queues) {
		return bindingQueue(
				queues != null ? MutableLists.newFastList(queues) : null, null);
	}

	public ExchangeAndBinding bindingQueue(List<Queue> queues, List<String> routingKeys) {
		if (queues != null) {
			queues.forEach(queue -> {
				if (CollectionUtils.isNotEmpty(routingKeys))
					routingKeys.forEach(routingKey -> bindings.add(new Binding(exchange, queue, routingKey)));
				else
					bindings.add(new Binding(exchange, queue));
			});
		}
		return this;
	}

	public static void main(String[] args) {
		ExchangeAndBinding.direct("TEST_DIRECT").exchangeAutoDelete(true).exchangeInternal(true);
	}

}