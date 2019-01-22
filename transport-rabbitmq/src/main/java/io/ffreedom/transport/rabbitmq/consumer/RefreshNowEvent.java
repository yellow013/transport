package io.ffreedom.transport.rabbitmq.consumer;

import java.util.function.Predicate;

/**
 * @author xuejian.sun
 * @date 2019-01-17 15:46
 */
public interface RefreshNowEvent<T> extends Predicate<T> {
    default boolean flushNow(T obj){
        return test(obj);
    }
}
