package io.ffreedom.transport.rabbitmq.consumer;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import io.ffreedom.common.log.ErrorLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Predicate;

/**
 * @author xuejian.sun
 * @date 2018/11/17 13:14
 */
public class QosBatchProcessConsumer<T> extends DefaultConsumer {

	private Logger log = LoggerFactory.getLogger(QosBatchProcessConsumer.class);

	private Channel channel;

	private QosBatchCallBack<List<T>> qosBatchCallBack;

	private QueueMessageSerializable<T> serializable;

	/**
	 * prefetchCount : rabbit consumer最多接受的数量
	 */
	private int prefetchCount;
	/**
	 * seconds: automatic flush check in the specified time
	 */
	private long millisSecond = 500L;
	/**
	 * 上一次的cache -> (linkedList) 大小
	 */
	private long lastSize;

	private LongAdder cacheSize;
	/**
	 * last rabbitmq message sequence
	 */
	private LongAdder lastDeliveryTag = new LongAdder();

	private List<T> linkedList;

	private Predicate<T> filter;

	private RefreshNowEvent<T> refreshNowEvent;

	private ScheduledExecutorService schedule;

	/**
	 * constructor
	 *
	 * @param channel          rabbit channel
	 * @param prefetchCount    从rmq获取prefetchCount数量的消息
	 * @param millisSecond     自动flush的时间间隔
	 * @param qosBatchCallBack 当达到prefetchCount值或自动flush触发此回调
	 */
	public QosBatchProcessConsumer(Channel channel, int prefetchCount, long millisSecond,
			QosBatchCallBack<List<T>> qosBatchCallBack, QueueMessageSerializable<T> serializable,
			RefreshNowEvent<T> refreshNowEvent, Predicate<T> filter) {
		super(channel);
		this.channel = channel;
		this.refreshNowEvent = refreshNowEvent;
		this.filter = filter;
		this.qosBatchCallBack = Objects.requireNonNull(qosBatchCallBack, "QosBatchCallBack can not be null");
		this.serializable = Objects.requireNonNull(serializable, "QueueMessageSerializable can not be null");
		this.prefetchCount = prefetchCount;
		if (millisSecond > 0) {
			this.millisSecond = millisSecond;
		} else {
			log.info("usr default seconds {}", this.millisSecond);
		}
		init();
	}

	public QosBatchProcessConsumer(Channel channel, QueueMessageSerializable<T> serializable,
			QosBatchCallBack<List<T>> qosBatchCallBack) {
		super(channel);
		this.channel = channel;
		this.qosBatchCallBack = Objects.requireNonNull(qosBatchCallBack, "QosBatchCallBack can not be null");
		this.serializable = Objects.requireNonNull(serializable, "QueueMessageSerializable can not be null");
		init();
	}

	private void init() {
		try {
			channel.basicQos(prefetchCount);
		} catch (IOException e) {
			log.error("set prefetchCount failure", e);
			return;
		}
		this.cacheSize = new LongAdder();
		linkedList = new LinkedList<>();
		ThreadFactory namedThreadFactory = new ThreadFactoryBuilder().setNameFormat("BatchHandlerAutoFlush-pool-%d")
				.build();
		schedule = new ScheduledThreadPoolExecutor(1, namedThreadFactory);
		automaticFlush();
	}

	@Override
	public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) {
		// 序列化
		T t = serializable.serialize(body);
		// 过滤器
		if (filter != null && !filter.test(t)) {
			return;
		}
		save(t, envelope.getDeliveryTag());
	}

	private synchronized void save(T t, long lastDeliveryTag) {
		this.lastDeliveryTag.add(lastDeliveryTag);
		if (Objects.nonNull(t)) {
			cacheSize.increment();
			linkedList.add(t);
		}
		if (cacheSize.longValue() >= prefetchCount) {
			log.info("The message to be stored reaches the threshold[{}] -> {} , deliveryTag[{}] ",
					cacheSize.longValue(), prefetchCount, this.lastDeliveryTag.longValue());
			flush();
		} else if (refreshNowEvent != null && refreshNowEvent.flushNow(t)) {
			log.info("----- 触发立刻刷新事件! tag -> {}-----", lastDeliveryTag);
			flush();
		}

	}

	/**
	 * batch flush queue message
	 */
	private synchronized void flush() {
		try {
			if (cacheSize.longValue() != 0) {
				Boolean batchResult = qosBatchCallBack.apply(linkedList);
				if (batchResult != null && batchResult) {
					channel.basicAck(lastDeliveryTag.longValue(), true);
					log.info("ack tag -> {}", lastDeliveryTag.longValue());
					linkedList.clear();
					cacheSize.reset();
					lastDeliveryTag.reset();
					log.info("cache clear, current size -> {}, cache -> {} , local tag -> {}", linkedList.size(),
							cacheSize.longValue(), lastDeliveryTag.longValue());
				}
			}
		} catch (IOException e) {
			// todo do reconnect and clear cache
			ErrorLogger.error(log, e);
		} catch (Exception e) {
			log.error("batch process failure,deliverTag[{}]", this.lastDeliveryTag.longValue(), e);
		}
	}

	/**
	 * schedule task : 如果缓存中的数据长期没有变动,触发flush动作 1.
	 * 当缓存队列中的值等于上一次定时任务触发的值,并且该值大于0,不等于rmq预取值,则认为该缓存数据长期没有变动,立即触发回调.
	 * <p>
	 * 缓存值达到预取值得大小交给主线程去触发刷新.减少锁的竞争.
	 */
	private void automaticFlush() {
		schedule.scheduleWithFixedDelay(() -> {
			try {
				if (cacheSize.longValue() == lastSize && cacheSize.longValue() > 0
						&& cacheSize.longValue() != prefetchCount) {
					log.info("automatic flush cache ...{} -> {} ,local deliveryTag[{}] ", cacheSize.longValue(),
							lastSize, lastDeliveryTag.longValue());
					flush();
				} else {
					lastSize = cacheSize.longValue();
				}
			} catch (Exception e) {
				log.error("automatic flush execute failure,deleveryTag[{}]", e, this.lastDeliveryTag.longValue());
			}
		}, 0, millisSecond, TimeUnit.MILLISECONDS);
	}

	@SuppressWarnings("unused")
	private void destroy() {
		if (!schedule.isShutdown()) {
			schedule.shutdown();
			schedule = null;
		}

		if (channel.isOpen()) {
			try {
				channel.close();
			} catch (IOException e) {
				log.error("close channel failure", e);
			} catch (TimeoutException e) {
				log.error("close channel timeout ", e);
			}
		}
		channel = null;
	}
}
