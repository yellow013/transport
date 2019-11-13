package io.ffreedom.transport.rabbitmq;

import java.io.IOException;
import java.util.List;
import java.util.function.Predicate;

import javax.annotation.Nonnull;

import io.ffreedom.transport.core.api.Receiver;
import io.ffreedom.transport.rabbitmq.config.RmqReceiverConfigurator;
import io.ffreedom.transport.rabbitmq.consumer.QosBatchCallBack;
import io.ffreedom.transport.rabbitmq.consumer.QosBatchProcessConsumer;
import io.ffreedom.transport.rabbitmq.consumer.QueueMessageSerializable;
import io.ffreedom.transport.rabbitmq.consumer.RefreshNowEvent;

/**
 * @author xuejian.sun
 * @date 2019/1/14 19:16
 */
public class RabbitQosBatchReceiver<T> extends BaseRabbitMqTransport implements Receiver {

	private String receiverName;

	private String receiveQueue;

	// 队列持久化
	private boolean durable = true;
	// 连接独占此队列
	private boolean exclusive = false;
	// channel关闭后自动删除队列
	private boolean autoDelete = false;

	private QosBatchProcessConsumer<T> consumer;

	public RabbitQosBatchReceiver(String tag, @Nonnull RmqReceiverConfigurator configurator, long autoFlushInterval,
			QueueMessageSerializable<T> serializable, QosBatchCallBack<List<T>> callBack,
			RefreshNowEvent<T> refreshNowEvent, Predicate<T> filter) {
		super(tag, "QosBatchReceiver", configurator.getConnectionConfigurator());
		this.receiveQueue = configurator.getQueueDeclare().getQueue().getName();
		createConnection();
		queueDeclare();
		consumer = new QosBatchProcessConsumer<T>(super.channel, configurator.getQos(), autoFlushInterval, callBack,
				serializable, refreshNowEvent, filter);
	}

	public RabbitQosBatchReceiver(String tag, @Nonnull RmqReceiverConfigurator configurator, long autoFlushInterval,
			QueueMessageSerializable<T> serializable, QosBatchCallBack<List<T>> callBack,
			RefreshNowEvent<T> refreshNowEvent) {
		super(tag, "QosBatchReceiver", configurator.getConnectionConfigurator());
		this.receiveQueue = configurator.getQueueDeclare().getQueue().getName();
		createConnection();
		queueDeclare();
		consumer = new QosBatchProcessConsumer<T>(super.channel, configurator.getQos(), autoFlushInterval, callBack,
				serializable, refreshNowEvent, null);
	}

	private void queueDeclare() {
		this.receiverName = "Receiver->" + connectionConfigurator.getConfiguratorName() + "$" + receiveQueue;
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
	public String getName() {
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
