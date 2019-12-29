package io.mercury.transport.rabbitmq;

import static io.mercury.common.utils.StringUtil.bytesToStr;

import java.io.IOException;
import java.time.DateTimeException;
import java.util.function.Consumer;

import javax.annotation.Nonnull;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

import io.mercury.common.character.Charsets;
import io.mercury.transport.core.api.Receiver;
import io.mercury.transport.rabbitmq.config.ConnectionConfigurator;
import io.mercury.transport.rabbitmq.config.RmqReceiverConfigurator;
import io.mercury.transport.rabbitmq.declare.ExchangeDeclare;
import io.mercury.transport.rabbitmq.declare.QueueDeclare;

/**
 * 
 * @author yellow013<br>
 * 
 *         改造升级, 使用共同的创建者建立Exchange, RoutingKey, Queue的绑定关系
 *
 */
public class RabbitMqReceiver extends BaseRabbitMqTransport implements Receiver {

	// 接收消息时使用的回调函数
	private volatile Consumer<byte[]> callback;

	// 接受者QueueDeclare
	private QueueDeclare queueDeclare;
	// 接受者Queue
	private String queueName;
	// 消息无法处理时发送到的错误消息ExchangeDeclare
	private ExchangeDeclare errorMsgExchange;
	// 消息无法处理时发送到的错误消息Exchange
	private String errorMsgExchangeName;
	// 是否有错误消息Exchange
	private boolean hasErrorMsgExchange;
	// 自动ACK
	private boolean autoAck;
	// 一次ACK多条
	private boolean multipleAck;
	// Ack最大自动重试次数
	private int maxAckTotal;
	// Ack最大自动重连次数
	private int maxAckReconnection;
	private int qos;

	private String receiverName;

	/**
	 * 
	 * @param configurator
	 */
	@Deprecated
	public RabbitMqReceiver(RmqReceiverConfigurator configurator) {
		this(null, configurator, null);
	}

	/**
	 * @param configurator
	 * @param callback
	 */
	@Deprecated
	public RabbitMqReceiver(String tag, RmqReceiverConfigurator configurator) {
		this(tag, configurator, null);
	}

	/**
	 * 
	 * @param callback
	 * @return
	 */
	@Deprecated
	public RabbitMqReceiver initCallback(Consumer<byte[]> callback) {
		if (this.callback == null)
			this.callback = callback;
		return this;
	}

	/**
	 * 
	 * @param configurator
	 * @param callback
	 */
	public RabbitMqReceiver(@Nonnull RmqReceiverConfigurator configurator, Consumer<byte[]> callback) {
		this(null, configurator, callback);
	}

	/**
	 * 
	 * @param tag
	 * @param configurator
	 * @param callback
	 */
	public RabbitMqReceiver(String tag, @Nonnull RmqReceiverConfigurator configurator, Consumer<byte[]> callback) {
		super(tag, "receiver", configurator.connectionConfigurator());
		this.callback = callback;
		this.queueDeclare = configurator.queueDeclare();
		this.errorMsgExchange = configurator.errorMsgExchange();
		this.autoAck = configurator.autoAck();
		this.multipleAck = configurator.multipleAck();
		this.maxAckTotal = configurator.maxAckTotal();
		this.maxAckReconnection = configurator.maxAckReconnection();
		this.qos = configurator.qos();
		createConnection();
		init();
	}

	private void init() {
		OperationalChannel operationalChannel = OperationalChannel.ofChannel(channel);
		try {
			this.queueDeclare.declare(operationalChannel);
		} catch (Exception e) {
			logger.error("Queue declare throw exception -> connection configurator info : {}, error message : {}",
					connectionConfigurator.name(), e.getMessage(), e);
			// 在定义Queue和进行绑定时抛出任何异常都需要终止程序
			destroy();
			throw new RuntimeException(e);
		}
		this.queueName = queueDeclare.queue().name();
		if (errorMsgExchange != null) {
			try {
				this.errorMsgExchange.declare(operationalChannel);
			} catch (Exception e) {
				logger.error(
						"ErrorMsgExchange declare throw exception -> connection configurator info : {}, error message : {}",
						connectionConfigurator.name(), e.getMessage(), e);
				// 在定义Queue和进行绑定时抛出任何异常都需要终止程序
				destroy();
				throw new RuntimeException(e);
			}
			this.errorMsgExchangeName = errorMsgExchange.exchange().name();
			this.hasErrorMsgExchange = true;
		}
		this.receiverName = "Receiver->" + connectionConfigurator.name() + "$" + queueName;
	}

	@Override
	public void receive() {
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
										"Callback handleDelivery() consumerTag==[{}], envelope.getDeliveryTag==[{}] body.length==[{}]",
										consumerTag, envelope.getDeliveryTag(), body.length);
								callback.accept(body);
								logger.debug("Callback handleDelivery() end");
							} catch (ArithmeticException e) {
								logger.error("Consumer accept msg==[{}] throw ArithmeticException -> {}",
										bytesToStr(body), e.getMessage(), e);
							} catch (DateTimeException e) {
								logger.error("Consumer accept msg==[{}] throw DateTimeException -> {}",
										bytesToStr(body), e.getMessage(), e);
							} catch (NumberFormatException e) {
								logger.error("Consumer accept msg==[{}] throw NumberFormatException -> {}",
										bytesToStr(body), e.getMessage(), e);
							} catch (Exception e) {
								logger.error("Consumer accept msg==[{}] throw Exception -> {}", bytesToStr(body),
										e.getMessage(), e);
								if (hasErrorMsgExchange) {
									// Sent message to error dump queue.
									logger.error("Exception handling -> Msg [{}] sent to ErrorMsgExchange",
											bytesToStr(body));
									channel.basicPublish(errorMsgExchangeName, "", null, body);
									logger.error("Exception handling -> Sent to ErrorMsgExchange finished");
								} else {
									// Reject message and close connection.
									logger.error("Exception handling -> Reject Msg [{}]", bytesToStr(body));
									channel.basicReject(envelope.getDeliveryTag(), true);
									logger.error("Exception handling -> Reject Msg finished");
									destroy();
								}
							}
							if (!autoAck) {
								if (ack(envelope.getDeliveryTag()))
									logger.debug("Message handle end");
								else {
									logger.info(
											"Call method ack() envelope.getDeliveryTag()==[{}] failure, Reject message");
									channel.basicReject(envelope.getDeliveryTag(), true);
								}
							}
						}
					});
		} catch (IOException e) {
			logger.error("Call method channel.basicConsume() IOException message -> {}", e.getMessage(), e);
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
		logger.info("Call method destroy() for Receiver tag==[{}]", tag);
		closeConnection();
		return true;
	}

	@Override
	public String name() {
		return receiverName;
	}

	public static void main(String[] args) {
		RabbitMqReceiver receiver = new RabbitMqReceiver("", RmqReceiverConfigurator
				.configuration(ConnectionConfigurator.configuration("", 5672, "", "").build(), QueueDeclare.named(""))
				.build(), msg -> System.out.println(new String(msg, Charsets.UTF8)));
		receiver.receive();
	}

	@Override
	public void reconnect() {
		closeAndReconnection();
		receive();
	}

}
