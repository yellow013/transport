package io.mercury.transport.rabbitmq.declare;

import java.util.Arrays;
import java.util.List;

import javax.annotation.Nonnull;

import io.mercury.transport.rabbitmq.DeclareOperator;
import io.mercury.transport.rabbitmq.exception.AmqpDeclareException;

/**
 * 定义Exchange和其他实体绑定关系
 * 
 * @author yellow013
 *
 */
public class ExchangeRelation extends Relation {

	public final static ExchangeRelation Anonymous = new ExchangeRelation(Exchange.Anonymous);

	private Exchange exchange;

	public static ExchangeRelation fanout(@Nonnull String exchangeName) {
		return new ExchangeRelation(Exchange.fanout(exchangeName));
	}

	public static ExchangeRelation direct(@Nonnull String exchangeName) {
		return new ExchangeRelation(Exchange.direct(exchangeName));
	}

	public static ExchangeRelation topic(@Nonnull String exchangeName) {
		return new ExchangeRelation(Exchange.topic(exchangeName));
	}

	public static ExchangeRelation withExchange(@Nonnull Exchange exchange) {
		return new ExchangeRelation(exchange);
	}

	private ExchangeRelation(Exchange exchange) {
		this.exchange = exchange;
	}

	@Override
	protected void declare0(DeclareOperator operator) {
		try {
			operator.declareExchange(exchange);
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

	public ExchangeRelation durable(boolean durable) {
		exchange.durable(durable);
		return this;
	}

	public ExchangeRelation autoDelete(boolean autoDelete) {
		exchange.autoDelete(autoDelete);
		return this;
	}

	public ExchangeRelation internal(boolean internal) {
		exchange.internal(internal);
		return this;
	}

	public ExchangeRelation bindingExchange(Exchange... exchanges) {
		return bindingExchange(exchanges != null ? Arrays.asList(exchanges) : null, null);
	}

	public ExchangeRelation bindingExchange(List<Exchange> exchanges, List<String> routingKeys) {
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

	public ExchangeRelation bindingQueue(Queue... queues) {
		return bindingQueue(queues != null ? Arrays.asList(queues) : null, null);
	}

	public ExchangeRelation bindingQueue(List<Queue> queues, List<String> routingKeys) {
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

		ExchangeRelation.direct("TEST_DIRECT").autoDelete(true).internal(true);

	}

}