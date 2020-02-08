package io.mercury.transport.rabbitmq.consumer;

import java.util.function.Function;

/**
 * @author xuejian.sun
 * @date 2018/11/20 11:28
 */
@FunctionalInterface
public interface QosBatchCallBack<T> extends Function<T, Boolean> {
	
}
