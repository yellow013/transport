package io.mercury.transport.rabbitmq;

import org.junit.Test;

import io.mercury.transport.rabbitmq.declare.ExchangeDeclare;

public class RabbitMqDeclareDSLTest {

	@Test
	public void test() {
		
		ExchangeDeclare.fanoutExchange("FAN_T1");
		
	
	}

}
