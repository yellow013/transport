package io.mercury.transport.rabbitmq;

import static io.mercury.common.util.StringUtil.bytesToStr;

import java.io.IOException;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.annotation.Nonnull;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

import io.mercury.common.character.Charsets;
import io.mercury.common.util.Assertor;
import io.mercury.transport.core.api.Receiver;
import io.mercury.transport.rabbitmq.configurator.RmqConnection;
import io.mercury.transport.rabbitmq.configurator.RmqReceiverConfigurator;
import io.mercury.transport.rabbitmq.declare.ExchangeRelation;
import io.mercury.transport.rabbitmq.declare.QueueRelation;
import io.mercury.transport.rabbitmq.exception.AmqpDeclareException;
import io.mercury.transport.rabbitmq.exception.AmqpDeclareRuntimeException;

/**
 * 
 * @author yellow013<br>
 * 
 *         [已完成]改造升级, 使用共同的创建者建立Exchange, RoutingKey, Queue的绑定关系
 *
 */
public class RabbitMqReceiver<T> extends AbstractRabbitMqTransport implements Receiver, Runnable {

	// 接收消息使用的反序列化器
	private Function<byte[], T> deserializer;

	// 接收消息时使用的回调函数
	private volatile Consumer<T> handler;

	// 接受者QueueDeclare
	private QueueRelation receiveQueue;

	// 接受者QueueName
	private String queueName;

	// 消息无法处理时发送到的错误消息ExchangeDeclare
	private ExchangeRelation errorMsgExchange;

	// 消息无法处理时发送到的错误消息Exchange使用的RoutingKey
	private String errorMsgRoutingKey;

	// 消息无法处理时发送到的错误消息QueueDeclare
	private QueueRelation errorMsgQueue;

	// 消息无法处理时发送到的错误消息Exchange
	private String errorMsgExchangeName;

	// 消息无法处理时发送到的错误消息Queue
	private String errorMsgQueueName;

	// 是否有错误消息Exchange
	private boolean hasErrorMsgExchange;

	// 是否有错误消息Queue
	private boolean hasErrorMsgQueue;

	// 自动ACK
	private boolean autoAck;

	// 一次ACK多条
	private boolean multipleAck;

	// ACK最大自动重试次数
	private int maxAckTotal;

	// ACK最大自动重连次数
	private int maxAckReconnection;

	// QOS预取
	private int qos;

	private String receiverName;

	/**
	 * 
	 * @param callback
	 * @return
	 */
	@Deprecated
	public void initHandler(Consumer<T> callback) {
		if (this.handler == null)
			this.handler = callback;
	}

	/**
	 * 
	 * @param configurator
	 * @return
	 */
	@Deprecated
	public static final RabbitMqReceiver<byte[]> create(@Nonnull RmqReceiverConfigurator configurator) {
		return new RabbitMqReceiver<byte[]>(null, configurator, msg -> msg, null);
	}

	/**
	 * 
	 * @param tag
	 * @param configurator
	 * @return
	 */
	@Deprecated
	public static final RabbitMqReceiver<byte[]> create(String tag, @Nonnull RmqReceiverConfigurator configurator) {
		return new RabbitMqReceiver<byte[]>(null, configurator, msg -> msg, null);
	}

	/**
	 * 
	 * @param configurator
	 * @param callback
	 * @return
	 */
	public static final RabbitMqReceiver<byte[]> create(@Nonnull RmqReceiverConfigurator configurator,
			@Nonnull Consumer<byte[]> handler) {
		return new RabbitMqReceiver<byte[]>(null, configurator, msg -> msg, handler);
	}

	/**
	 * 
	 * @param <T>
	 * @param configurator
	 * @param deserializer
	 * @param callback
	 * @return
	 */
	public static final <T> RabbitMqReceiver<T> create(@Nonnull RmqReceiverConfigurator configurator,
			@Nonnull Function<byte[], T> deserializer, @Nonnull Consumer<T> handler) {
		return new RabbitMqReceiver<T>(null, configurator, deserializer, handler);
	}

	/**
	 * 
	 * @param tag
	 * @param configurator
	 * @param callback
	 * @return
	 */
	public static final RabbitMqReceiver<byte[]> create(String tag, @Nonnull RmqReceiverConfigurator configurator,
			@Nonnull Consumer<byte[]> handler) {
		return new RabbitMqReceiver<byte[]>(tag, configurator, msg -> msg, handler);
	}

	/**
	 * 
	 * @param <T>
	 * @param tag
	 * @param configurator
	 * @param deserializer
	 * @param callback
	 * @return
	 */
	public static final <T> RabbitMqReceiver<T> create(String tag, @Nonnull RmqReceiverConfigurator configurator,
			@Nonnull Function<byte[], T> deserializer, @Nonnull Consumer<T> handler) {
		return new RabbitMqReceiver<T>(tag, configurator, deserializer, handler);
	}

	/**
	 * 
	 * @param tag
	 * @param configurator
	 * @param deserializer
	 * @param callback
	 */
	private RabbitMqReceiver(String tag, @Nonnull RmqReceiverConfigurator configurator,
			@Nonnull Function<byte[], T> deserializer, @Nonnull Consumer<T> handler) {
		super(tag, "receiver", configurator.connection());
		this.deserializer = deserializer;
		this.handler = handler;
		this.receiveQueue = configurator.receiveQueue();
		this.errorMsgExchange = configurator.errorMsgExchange();
		this.errorMsgRoutingKey = configurator.errorMsgRoutingKey();
		this.errorMsgQueue = configurator.errorMsgQueue();
		this.autoAck = configurator.autoAck();
		this.multipleAck = configurator.multipleAck();
		this.maxAckTotal = configurator.maxAckTotal();
		this.maxAckReconnection = configurator.maxAckReconnection();
		this.qos = configurator.qos();
		createConnection();
		declare();
		this.receiverName = "receiver::" + rmqConnection.fullInfo() + "$" + queueName;
	}

	private void declare() {
		DeclareOperator operator = DeclareOperator.ofChannel(channel);
		try {
			this.receiveQueue.declare(operator);
		} catch (Exception e) {
			logger.error("Queue declare throw exception -> connection configurator info : {}, error message : {}",
					rmqConnection.fullInfo(), e.getMessage(), e);
			// 在定义Queue和进行绑定时抛出任何异常都需要终止程序
			destroy();
			throw new RuntimeException(e);
		}
		this.queueName = receiveQueue.queueName();
		if (errorMsgExchange != null && errorMsgQueue != null) {
			errorMsgExchange.bindingQueue(errorMsgQueue.queue());
			declareErrorMsgExchange(operator);
		} else if (errorMsgExchange != null) {
			declareErrorMsgExchange(operator);
		} else if (errorMsgQueue != null) {
			declareErrorMsgQueueName(operator);
		}

	}

	private void declareErrorMsgExchange(DeclareOperator opChannel) {
		try {
			this.errorMsgExchange.declare(opChannel);
		} catch (AmqpDeclareException e) {
			logger.error(
					"ErrorMsgExchange declare throw exception -> connection configurator info : {}, error message : {}",
					rmqConnection.fullInfo(), e.getMessage(), e);
			// 在定义Queue和进行绑定时抛出任何异常都需要终止程序
			destroy();
			throw new AmqpDeclareRuntimeException(e);
		}
		this.errorMsgExchangeName = errorMsgExchange.exchangeName();
		this.hasErrorMsgExchange = true;
	}

	private void declareErrorMsgQueueName(DeclareOperator operator) {
		try {
			this.errorMsgQueue.declare(operator);
		} catch (AmqpDeclareException e) {
			logger.error(
					"ErrorMsgQueue declare throw exception -> connection configurator info : {}, error message : {}",
					rmqConnection.fullInfo(), e.getMessage(), e);
			// 在定义Queue和进行绑定时抛出任何异常都需要终止程序
			destroy();
			throw new AmqpDeclareRuntimeException(e);
		}
		this.errorMsgQueueName = errorMsgQueue.queueName();
		this.hasErrorMsgQueue = true;
	}

	@Override
	public void run() {
		receive();
	}

	@Override
	public void receive() {
		Assertor.nonNull(deserializer, "deserializer");
		Assertor.nonNull(handler, "handler");
		try {
			// channel.basicConsume(receiveQueue, isAutoAck, tag, (consumerTag, msg) -> {
			// Envelope envelope = msg.getEnvelope();
			// }, (consumerTag) -> {
			// }, (consumerTag, shutdownException) -> {
			// });
			if (!autoAck)
				channel.basicQos(qos);
			channel.basicConsume(
					// param1: queue
					queueName,
					// param2: autoAck
					autoAck,
					// param3: consumerTag
					tag,
					// param4: consumeCallback
					new DefaultConsumer(channel) {
						@Override
						public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties,
								byte[] body) throws IOException {
							try {
								logger.debug("Message handle start");
								logger.debug(
										"Callback handleDelivery() consumerTag==[{}], deliveryTag==[{}] body.length==[{}]",
										consumerTag, envelope.getDeliveryTag(), body.length);
								handler.accept(deserializer.apply(body));
								logger.debug("Callback handleDelivery() end");
							} catch (Exception e) {
								logger.error("Consumer accept msg==[{}] throw Exception -> {}", bytesToStr(body),
										e.getMessage(), e);
								errorDump(consumerTag, envelope, properties, body);
							}
							if (!autoAck) {
								if (ack(envelope.getDeliveryTag()))
									logger.debug("Message handle and ack finished");
								else {
									logger.info("Ack failure envelope.getDeliveryTag()==[{}], Reject message");
									channel.basicReject(envelope.getDeliveryTag(), true);
								}
							}
						}
					});
		} catch (IOException e) {
			logger.error("Method basicConsume() IOException message -> {}", e.getMessage(), e);
		}
	}

	private void errorDump(String consumerTag, Envelope envelope, BasicProperties properties, byte[] body)
			throws IOException {
		if (hasErrorMsgExchange) {
			// Sent message to error dump exchange.
			logger.error("Exception handling -> Sent to ErrorMsgExchange [{}]", errorMsgExchangeName);
			channel.basicPublish(errorMsgExchangeName, errorMsgRoutingKey, null, body);
			logger.error("Exception handling -> Sent to ErrorMsgExchange [{}] finished", errorMsgExchangeName);
		} else if (hasErrorMsgQueue) {
			// Sent message to error dump queue.
			logger.error("Exception handling -> Sent to ErrorMsgQueue [{}]", errorMsgQueueName);
			channel.basicPublish("", errorMsgQueueName, null, body);
			logger.error("Exception handling -> Sent to ErrorMsgQueue finished");
		} else {
			// Reject message and close connection.
			logger.error("Exception handling -> Reject Msg [{}]", bytesToStr(body));
			channel.basicReject(envelope.getDeliveryTag(), true);
			logger.error("Exception handling -> Reject Msg finished");
			destroy();
			throw new RuntimeException(
					"The message could not handle, and could not delivered to the error dump address. "
							+ "\n The connection was closed.");
		}
	}

	private boolean ack(long deliveryTag) {
		return ack0(deliveryTag, 0);
	}

	private boolean ack0(long deliveryTag, int retry) {
		if (retry == maxAckTotal) {
			logger.error("Has been retry ack {}, Quit ack", maxAckTotal);
			return false;
		}
		logger.debug("Has been retry ack {}, Do next ack", retry);
		try {
			int reconnectionCount = 0;
			while (!isConnected()) {
				reconnectionCount++;
				logger.debug("Detect connection isConnected() == false, Reconnection count {}", reconnectionCount);
				closeAndReconnection();
				if (reconnectionCount > maxAckReconnection) {
					logger.debug("Reconnection count -> {}, Quit current ack", reconnectionCount);
					break;
				}
			}
			if (isConnected()) {
				logger.debug("Last detect connection isConnected() == true, Reconnection count {}", reconnectionCount);
				channel.basicAck(deliveryTag, multipleAck);
				logger.debug("Method channel.basicAck() finished");
				return true;
			} else {
				logger.error("Last detect connection isConnected() == false, Reconnection count {}", reconnectionCount);
				logger.error("Unable to call method channel.basicAck()");
				return ack0(deliveryTag, retry);
			}
		} catch (IOException e) {
			logger.error("Call method channel.basicAck(deliveryTag==[{}], multiple==[{}]) throw IOException -> {}",
					deliveryTag, multipleAck, e.getMessage(), e);
			return ack0(deliveryTag, ++retry);
		}
	}

	@Override
	public boolean destroy() {
		logger.info("Call method destroy() from Receiver name==[{}]", receiverName);
		return super.destroy();
	}

	@Override
	public String name() {
		return receiverName;
	}

	@Override
	public void reconnect() {
		closeAndReconnection();
		receive();
	}

	public static void main(String[] args) {
		RabbitMqReceiver<byte[]> receiver = RabbitMqReceiver.create("test", RmqReceiverConfigurator
				.configuration(RmqConnection.configuration("", 5672, "", "").build(), QueueRelation.named("")).build(),
				msg -> System.out.println(new String(msg, Charsets.UTF8)));
		receiver.receive();
	}

}
