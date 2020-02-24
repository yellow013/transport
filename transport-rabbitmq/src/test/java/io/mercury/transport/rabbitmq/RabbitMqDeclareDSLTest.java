package io.mercury.transport.rabbitmq;

import org.junit.Test;

import io.mercury.transport.rabbitmq.declare.ExchangeAndBinding;

public class RabbitMqDeclareDSLTest {

	@Test
	public void test() {
		
		ExchangeAndBinding.fanout("FAN_T1");
		
	}

}
