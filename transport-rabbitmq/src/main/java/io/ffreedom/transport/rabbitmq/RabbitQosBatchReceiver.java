package io.ffreedom.transport.rabbitmq;

import io.ffreedom.common.log.ErrorLogger;
import io.ffreedom.transport.core.role.Receiver;
import io.ffreedom.transport.rabbitmq.config.ReceiverConfigurator;
import io.ffreedom.transport.rabbitmq.consumer.QosBatchCallBack;
import io.ffreedom.transport.rabbitmq.consumer.QosBatchProcessConsumer;
import io.ffreedom.transport.rabbitmq.consumer.QueueMessageSerializable;
import io.ffreedom.transport.rabbitmq.consumer.RefreshNowEvent;

import java.io.IOException;
import java.util.List;
import java.util.function.Predicate;

/**
 * @author xuejian.sun
 * @date 2019/1/14 19:16
 */
public class RabbitQosBatchReceiver<T> extends BaseRabbitMqTransport<ReceiverConfigurator> implements Receiver {

    private String receiverName;

    private String queueName;

    private QosBatchProcessConsumer<T> consumer;

    public RabbitQosBatchReceiver(String tag, ReceiverConfigurator configurator, long autoFlushInterval
            , QueueMessageSerializable<T> serializable, QosBatchCallBack<List<T>> callBack
            , RefreshNowEvent<T> refreshNowEvent, Predicate<T> filter) {
        super(tag, configurator);
        this.queueName = configurator.getReceiveQueue();
        createConnection();
        queueDeclare();
        consumer = new QosBatchProcessConsumer<T>(super.channel, configurator.getQos()
                , autoFlushInterval, callBack, serializable,refreshNowEvent,filter);
    }

    public RabbitQosBatchReceiver(String tag, ReceiverConfigurator configurator, long autoFlushInterval
            , QueueMessageSerializable<T> serializable, QosBatchCallBack<List<T>> callBack
            , RefreshNowEvent<T> refreshNowEvent) {
        super(tag, configurator);
        this.queueName = configurator.getReceiveQueue();
        createConnection();
        queueDeclare();
        consumer = new QosBatchProcessConsumer<T>(super.channel, configurator.getQos()
                , autoFlushInterval, callBack, serializable,refreshNowEvent,null);
    }

    private void queueDeclare() {
        this.receiverName = "Receiver->" + configurator.getHost() + ":" + configurator.getPort() + "$" + queueName;
        try {
            channel.queueDeclare(queueName, configurator.isDurable(), configurator.isExclusive(),
                    configurator.isAutoDelete(), null);
        } catch (IOException e) {
            ErrorLogger.error(logger, e,
                    "Method channel.queueDeclare(queue==[{}], durable==[{]}, exclusive==[{}], autoDelete==[{}], arguments==null) IOException message -> {}",
                    queueName, configurator.isDurable(), configurator.isExclusive(), configurator.isAutoDelete(),
                    e.getMessage());
            destroy();
        }
    }

    @Override
    public void receive() {
        try {
            channel.basicConsume(queueName, false, tag, consumer);
        } catch (IOException e) {
            ErrorLogger.error(logger, e, "consumer error", e.getMessage());
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
