package io.ffreedom.transport.rabbitmq;

import io.ffreedom.transport.rabbitmq.config.ConnectionConfigurator;
import io.ffreedom.transport.rabbitmq.config.PublisherConfigurator;
import io.ffreedom.transport.rabbitmq.config.ReceiverConfigurator;

public class RabbitMqPublisherTest {

	public static void main(String[] args) {

		ConnectionConfigurator connectionConfigurator = ConnectionConfigurator.configuration("10.0.64.201", 5672,
				"global", "global2018", "report");

		RabbitMqPublisher publisher = new RabbitMqPublisher("test-publisher",
				PublisherConfigurator.configuration(connectionConfigurator));

		new RabbitMqReceiver("test-receiver",
				ReceiverConfigurator.configuration(connectionConfigurator).setReceiveQueue(""),
				byteMsg -> System.out.println(new String(byteMsg))).receive();

	}

}
