package io.ffreedom.transport.rabbitmq;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.AMQP.Queue.DeleteOk;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;

import io.ffreedom.transport.rabbitmq.config.ConnectionConfigurator;

public final class RabbitMqOperatingTools {

	public static OperationalChannel createChannel(String host, int port, String username, String password)
			throws IOException, TimeoutException {
		return new OperationalChannel("OperationalChannel",
				ConnectionConfigurator.configuration(host, port, username, password));
	}

	public static OperationalChannel createChannel(ConnectionConfigurator configurator)
			throws IOException, TimeoutException {
		return new OperationalChannel("OperationalChannel", configurator);
	}

	public static OperationalChannel ofChannel(Channel channel) {
		return new OperationalChannel(channel);
	}

	public static class OperationalChannel extends BaseRabbitMqTransport {

		private OperationalChannel(String tag, ConnectionConfigurator configurator)
				throws IOException, TimeoutException {
			super(tag, configurator);
			createConnection();
		}

		private OperationalChannel(Channel channel) {
			this.channel = channel;
		}

		/**
		 * 
		 * @param String           -> queue name
		 * @param DefaultParameter -> durable == true, exclusive == false, autoDelete ==
		 *                         false<br>
		 * @throws IOException
		 */
		public boolean declareQueueDefault(String queue) throws IOException {
			return declareQueue(queue, true, false, false);
		}

		public boolean declareQueue(String queue, boolean durable, boolean exclusive, boolean autoDelete)
				throws IOException {
			channel.queueDeclare(queue, durable, exclusive, autoDelete, null);
			return true;
		}

		/**
		 * 
		 * @param exchange
		 * @param DefaultParameter -> durable == true, autoDelete == false, internal ==
		 *                         false
		 * @return
		 * @throws IOException
		 */
		public boolean declareDirectExchangeDefault(String exchange) throws IOException {
			return declareDirectExchange(exchange, true, false, false);
		}

		public boolean declareDirectExchange(String exchange, boolean durable, boolean autoDelete, boolean internal)
				throws IOException {
			return declareExchange(exchange, BuiltinExchangeType.DIRECT, durable, autoDelete, internal);
		}

		public boolean declareFanoutExchangeDefault(String exchange) throws IOException {
			return declareFanoutExchange(exchange, true, false, false);
		}

		public boolean declareFanoutExchange(String exchange, boolean durable, boolean autoDelete, boolean internal)
				throws IOException {
			return declareExchange(exchange, BuiltinExchangeType.FANOUT, durable, autoDelete, internal);
		}

		public boolean declareTopicExchangeDefault(String exchange) throws IOException {
			return declareTopicExchange(exchange, true, false, false);
		}

		public boolean declareTopicExchange(String exchange, boolean durable, boolean autoDelete, boolean internal)
				throws IOException {
			return declareExchange(exchange, BuiltinExchangeType.TOPIC, durable, autoDelete, internal);
		}

		private boolean declareExchange(String exchange, BuiltinExchangeType type, boolean durable, boolean autoDelete,
				boolean internal) throws IOException {
			channel.exchangeDeclare(exchange, type, durable, autoDelete, internal, null);
			return true;
		}

		public boolean bindQueue(String queue, String exchange) throws IOException {
			return bindQueue(queue, exchange, "");
		}

		public boolean bindQueue(String queue, String exchange, String routingKey) throws IOException {
			channel.queueBind(queue, exchange, routingKey);
			return true;
		}

		public boolean deleteQueue(String queue, boolean force) throws IOException {
			DeleteOk queueDelete = channel.queueDelete(queue, !force, !force);
			queueDelete.getMessageCount();
			return true;
		}

		public boolean deleteExchange(String exchange, boolean force) throws IOException {
			channel.exchangeDelete(exchange, !force);
			return true;
		}

		public boolean isOpen() {
			return channel.isOpen();
		}

		public boolean close() throws IOException, TimeoutException {
			channel.close();
			connection.close();
			return true;
		}

		@Override
		public String getName() {
			return tag;
		}

		@Override
		public boolean destroy() {
			return false;
		}
	}

//	static class TestBean {
//		private long L;
//		private int I;
//		private double D;
//		private String S;
//
//		public long getL() {
//			return L;
//		}
//
//		public TestBean setL(long l) {
//			L = l;
//			return this;
//		}
//
//		public int getI() {
//			return I;
//		}
//
//		public TestBean setI(int i) {
//			I = i;
//			return this;
//		}
//
//		public double getD() {
//			return D;
//		}
//
//		public TestBean setD(double d) {
//			D = d;
//			return this;
//		}
//
//		public String getS() {
//			return S;
//		}
//
//		public TestBean setS(String s) {
//			S = s;
//			return this;
//		}
//
//	}

	public static void main(String[] args) {

//		MutableList<TestBean> list = new FastList<>();
//
//		list.add(new TestBean().setI(1).setD(3.0D).setL(2L).setS("AT"));
//		list.add(new TestBean().setI(1).setD(4.0D).setL(5L).setS("AT"));
//		list.add(new TestBean().setI(2).setD(3.0D).setL(5L).setS("AS"));
//		list.add(new TestBean().setI(2).setD(4.0D).setL(5L).setS("AT"));
//
//		list.collectDouble(bean -> {
//			return bean.getD();
//		}).distinct().forEach(d -> {
//			System.out.println(d * 2);
//		});
//
//		MutableList<TestBean> collect = list.distinctBy(TestBean::getS);
//
//		collect.forEach(bean -> {
//			System.out.println(bean.getS());
//		});

		OperationalChannel manualCloseChannel;
		try {
			manualCloseChannel = createChannel("127.0.0.1", 5672, "guest", "guest");
			System.out.println(manualCloseChannel.isOpen());
			manualCloseChannel.declareFanoutExchange("MarketData", true, false, false);
			manualCloseChannel.close();
			System.out.println(manualCloseChannel.isOpen());
		} catch (IOException | TimeoutException e) {

		}

	}

}
