package io.mercury.transport.rabbitmq;

import java.io.IOException;
import java.util.List;
import java.util.function.Predicate;

import javax.annotation.Nonnull;

import io.mercury.transport.core.api.Receiver;
import io.mercury.transport.rabbitmq.configurator.RmqReceiverConfigurator;
import io.mercury.transport.rabbitmq.consumer.QosBatchCallBack;
import io.mercury.transport.rabbitmq.consumer.QosBatchProcessConsumer;
import io.mercury.transport.rabbitmq.consumer.QueueMessageSerializable;
import io.mercury.transport.rabbitmq.consumer.RefreshNowEvent;

/**
 * @author xuejian.sun
 * @date 2019/1/14 19:16
 */
public class RabbitMqQosBatchReceiver<T> extends AbstractRabbitMqTransport implements Receiver {

	private String receiverName;

	private String receiveQueue;

	// 队列持久化
	private boolean durable = true;
	// 连接独占此队列
	private boolean exclusive = false;
	// channel关闭后自动删除队列
	private boolean autoDelete = false;

	private QosBatchProcessConsumer<T> consumer;

	public RabbitMqQosBatchReceiver(String tag, @Nonnull RmqReceiverConfigurator configurator, long autoFlushInterval,
			QueueMessageSerializable<T> serializable, QosBatchCallBack<List<T>> callBack,
			RefreshNowEvent<T> refreshNowEvent, Predicate<T> filter) {
		super(tag, "QosBatchReceiver", configurator.connection());
		this.receiveQueue = configurator.queueDeclare().queue().name();
		createConnection();
		queueDeclare();
		consumer = new QosBatchProcessConsumer<T>(super.channel, configurator.qos(), autoFlushInterval, callBack,
				serializable, refreshNowEvent, filter);
	}

	public RabbitMqQosBatchReceiver(String tag, @Nonnull RmqReceiverConfigurator configurator, long autoFlushInterval,
			QueueMessageSerializable<T> serializable, QosBatchCallBack<List<T>> callBack,
			RefreshNowEvent<T> refreshNowEvent) {
		super(tag, "QosBatchReceiver", configurator.connection());
		this.receiveQueue = configurator.queueDeclare().queue().name();
		createConnection();
		queueDeclare();
		consumer = new QosBatchProcessConsumer<T>(super.channel, configurator.qos(), autoFlushInterval, callBack,
				serializable, refreshNowEvent, null);
	}

	private void queueDeclare() {
		this.receiverName = "Receiver->" + rmqConnection.fullInfo() + "$" + receiveQueue;
		try {
			channel.queueDeclare(receiveQueue, durable, exclusive, autoDelete, null);
		} catch (IOException e) {
			logger.error(
					"Method channel.queueDeclare(queue==[{}], durable==[{]}, exclusive==[{}], autoDelete==[{}], arguments==null) IOException message -> {}",
					receiveQueue, durable, exclusive, autoDelete, e.getMessage(), e);
			destroy();
		}
	}

	@Override
	public void receive() {
		try {
			channel.basicConsume(receiveQueue, false, tag, consumer);
		} catch (IOException e) {
			logger.error("basicConsume error", e.getMessage(), e);
		}
	}

	@Override
	public String name() {
		return receiverName;
	}

	@Override
	public boolean destroy() {
		logger.info("Call method RabbitMqReceiver.destroy()");
		closeConnection();
		return true;
	}

	@Override
	public void reconnect() {
		// TODO Auto-generated method stub

	}
}
