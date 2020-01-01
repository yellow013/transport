package io.mercury.transport.rabbitmq;

import java.util.Arrays;

import io.mercury.transport.rabbitmq.config.ConnectionConfigurator;
import io.mercury.transport.rabbitmq.config.RmqPublisherConfigurator;
import io.mercury.transport.rabbitmq.declare.AmqpEntity.Queue;
import io.mercury.transport.rabbitmq.declare.ExchangeDeclare;

public class RabbitMqPublisherTest {

	public static void main(String[] args) {

		ConnectionConfigurator connectionConfigurator = ConnectionConfigurator
				.configuration("10.0.64.201", 5672, "global", "global2018").build();

		RmqPublisherConfigurator publisherConfigurator = RmqPublisherConfigurator
				.configuration(connectionConfigurator, ExchangeDeclare.fanout("TEST_DIR")
						.bindingQueue(Arrays.asList(Queue.named("TEST_D1")), Arrays.asList("K1", "K2")))
				.defaultRoutingKey("K1").build();

		RabbitMqPublisher publisher = new RabbitMqPublisher("TEST_PUB", publisherConfigurator);

		publisher.publish(new String("To_K1").getBytes());
		publisher.publish("K1", new String("To_K1_0").getBytes());
		publisher.publish("K2", new String("To_K2").getBytes());

		publisher.destroy();

	}

}
