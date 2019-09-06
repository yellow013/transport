package io.ffreedom.transport.rabbitmq;

import java.io.IOException;
import java.util.function.Consumer;

import javax.annotation.Nonnull;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

import io.ffreedom.common.charset.Charsets;
import io.ffreedom.common.log.ErrorLogger;
import io.ffreedom.common.utils.StringUtil;
import io.ffreedom.transport.core.role.Receiver;
import io.ffreedom.transport.rabbitmq.config.ConnectionConfigurator;
import io.ffreedom.transport.rabbitmq.config.ReceiverConfigurator;

public class RabbitMqReceiver2 extends BaseRabbitMqTransport implements Receiver {

	// 接收消息时使用的回调函数
	private volatile Consumer<byte[]> callback;

	// 绑定的Exchange
	// 暂时没有使用
	@SuppressWarnings("unused")
	private String exchange[];
	@SuppressWarnings("unused")
	private String[] routingKey;
	// 连接的Queue
	private String receiveQueue;

	// 消息无法处理时发送到的错误队列
	private String errorMsgToExchange;

	// 队列持久化
	private boolean durable;
	// 连接独占此队列
	private boolean exclusive;
	// channel关闭后自动删除队列
	private boolean autoDelete;
	// 自动ACK
	private boolean autoAck;
	// 一次ACK多条
	private boolean multipleAck;
	// 最大自动重试次数
	private int maxAckTotal;
	private int maxAckReconnection;
	private String receiverName;

	@SuppressWarnings("unused")
	private int qos;

	/**
	 * 
	 * @param configurator
	 * @param callback
	 */
	public RabbitMqReceiver2(String tag, @Nonnull ReceiverConfigurator configurator, Consumer<byte[]> callback) {
		super(tag, configurator.getConnectionConfigurator());
		this.callback = callback;
		this.exchange = configurator.getExchange();
		this.routingKey = configurator.getRoutingKey();
		this.receiveQueue = configurator.getReceiveQueue();
		this.durable = configurator.isDurable();
		this.exclusive = configurator.isExclusive();
		this.autoDelete = configurator.isAutoDelete();
		this.autoAck = configurator.isAutoAck();
		this.multipleAck = configurator.isMultipleAck();
		this.maxAckTotal = configurator.getMaxAckTotal();
		this.maxAckReconnection = configurator.getMaxAckReconnection();
		this.errorMsgToExchange = configurator.getErrorMsgToExchange();
		this.qos = configurator.getQos();
		createConnection();
		init();
	}

	/**
	 * 
	 * @param configurator
	 * @param callback
	 */
	@Deprecated
	public RabbitMqReceiver2(String tag, ReceiverConfigurator configurator) {
		this(tag, configurator, null);
	}

	private void init() {
		this.receiverName = "Receiver->" + connectionConfigurator.getConfiguratorName() + "$" + receiveQueue;
		try {
			channel.queueDeclare(receiveQueue, durable, exclusive, autoDelete, null);
		} catch (IOException e) {
			ErrorLogger.error(logger, e,
					"Method channel.queueDeclare(queue==[{}], durable==[{]}, exclusive==[{}], autoDelete==[{}], arguments==null) IOException message -> {}",
					receiveQueue, durable, exclusive, autoDelete, e.getMessage());
			destroy();
		}
	}

	@Override
	public void receive() {
		try {
			// channel.basicConsume(receiveQueue, isAutoAck, tag, (consumerTag, msg) -> {
			// Envelope envelope = msg.getEnvelope();
			// }, (consumerTag) -> {
			// }, (consumerTag, shutdownException) -> {
			// });

			// param1: queue
			// param2: autoAck
			// param3: consumeCallback
			channel.basicConsume(receiveQueue, autoAck, tag, new DefaultConsumer(channel) {
				@Override
				public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties,
						byte[] body) throws IOException {
					try {
						logger.debug("Message handle start.");
						logger.debug(
								"Callback handleDelivery(consumerTag==[{}], envelope.getDeliveryTag==[{}] body.length==[{}])",
								consumerTag, envelope.getDeliveryTag(), body.length);
						callback.accept(body);
						logger.debug("Callback handleDelivery() end.");
					} catch (Exception e) {
						ErrorLogger.error(logger, e, "Call method callback.accept(body) throw Exception -> {}",
								e.getMessage());
						if (StringUtil.notNullAndEmpty(errorMsgToExchange)) {
							// Sent message to error dump queue.
							logger.info("Exception handling -> Msg [{}] sent to ErrorMsgExchange.",
									new String(body, Charsets.UTF8));
							channel.basicPublish(errorMsgToExchange, "", null, body);
							logger.info("Exception handling -> Sent to ErrorMsgExchange finished.");
						} else {
							// Reject message and close connection.
							logger.info("Exception handling -> Reject Msg [{}]", new String(body, Charsets.UTF8));
							channel.basicReject(envelope.getDeliveryTag(), true);
							logger.info("Exception handling -> Reject Msg finished.");
							destroy();
						}
					}
					if (!autoAck) {
						if (ack(envelope.getDeliveryTag()))
							logger.debug("Message handle end.");
						else {
							logger.info("Call method ack(envelope.getDeliveryTag()==[{}]) failure. Reject message.");
							channel.basicReject(envelope.getDeliveryTag(), true);
						}
					}
				}
			});
		} catch (IOException e) {
			ErrorLogger.error(logger, e, "Call method channel.basicConsume() IOException message -> {}",
					e.getMessage());
		}
	}

	private boolean ack(long deliveryTag) {
		return ack0(deliveryTag, 0);
	}

	private boolean ack0(long deliveryTag, int retry) {
		if (retry == maxAckTotal) {
			logger.error("Has been retry ack {}, Quit ack.", maxAckTotal);
			return false;
		}
		logger.debug("Has been retry ack {}, Do next ack.", retry);
		try {
			int reconnectionCount = 0;
			while (!isConnected()) {
				logger.debug("Detect connection isConnected() == false, Reconnection count {}.", (++reconnectionCount));
				closeAndReconnection();
				if (reconnectionCount == maxAckReconnection) {
					logger.debug("Reconnection count -> {}, Quit current ack.", reconnectionCount);
					break;
				}
			}
			if (isConnected()) {
				logger.debug("Last detect connection isConnected() == true, Reconnection count {}", reconnectionCount);
				channel.basicAck(deliveryTag, multipleAck);
				logger.debug("Method channel.basicAck() finished.");
				return true;
			} else {
				logger.error("Last detect connection isConnected() == false, Reconnection count {}", reconnectionCount);
				logger.error("Unable to call method channel.basicAck()");
				return ack0(deliveryTag, retry);
			}
		} catch (IOException e) {
			ErrorLogger.error(logger, e,
					"Call method channel.basicAck(deliveryTag==[{}], multiple==[{}]) throw IOException -> {}",
					deliveryTag, multipleAck, e.getMessage());
			return ack0(deliveryTag, ++retry);
		}

	}

	@Override
	public boolean destroy() {
		logger.info("Call method RabbitMqReceiver.destroy()");
		closeConnection();
		return true;
	}

	@Override
	public String getName() {
		return receiverName;
	}

	@Deprecated
	public boolean initCallback(Consumer<byte[]> callback) {
		if (this.callback != null)
			return false;
		this.callback = callback;
		return true;
	}

	public static void main(String[] args) {
		RabbitMqReceiver2 receiver = new RabbitMqReceiver2("",
				ReceiverConfigurator.configuration(ConnectionConfigurator.configuration("", 5672, "", "").build()).setReceiveQueue(""),
				msg -> System.out.println(new String(msg, Charsets.UTF8)));
		receiver.receive();
	}

	@Override
	public void reconnect() {
		closeAndReconnection();
		receive();
	}

}
