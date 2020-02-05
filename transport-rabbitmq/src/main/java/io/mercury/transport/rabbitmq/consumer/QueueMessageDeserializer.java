package io.mercury.transport.rabbitmq.consumer;

import java.util.function.Function;

/**
 * @author xuejian.sun
 * @date 2018/11/20 14:44
 */
@FunctionalInterface
public interface QueueMessageSerializable<R> extends Function<byte[], R> {

	/**
	 * convert byte[] to R
	 *
	 * @param bytes bytes
	 * @return R
	 */
	default R serialize(byte[] bytes) {
		return apply(bytes);
	}
	
}
