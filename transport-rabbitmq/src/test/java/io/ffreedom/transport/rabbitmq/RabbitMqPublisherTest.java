package io.ffreedom.transport.rabbitmq;

import io.ffreedom.transport.rabbitmq.config.PublisherConfigurator;

public class RabbitMqPublisherTest {

	public static void main(String[] args) {

		RabbitMqPublisher publisher = new RabbitMqPublisher("test-publisher",
				PublisherConfigurator.configuration("10.0.64.201", 5672, "global", "global2018"));

	}

}
