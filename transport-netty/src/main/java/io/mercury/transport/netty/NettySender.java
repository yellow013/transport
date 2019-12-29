package io.mercury.transport.netty;

import static io.mercury.common.datetime.SysSequence.microsecond;

import org.slf4j.Logger;

import io.mercury.common.log.CommonLoggerFactory;
import io.mercury.transport.core.api.Sender;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;

public class NettySender implements Sender<byte[]> {

	private ChannelHandlerContext context;

	// private ByteBuf byteBuf;

	private Logger logger = CommonLoggerFactory.getLogger(getClass());

	public NettySender(ChannelHandlerContext context) {
		this.context = context;
		// this.byteBuf = byteBuf;
	}

	// public NettySender(ChannelHandlerContext context) {
	// this(context);
	// }

	@Override
	public boolean isConnected() {
		context.disconnect();
		return true;
	}

	@Override
	public boolean destroy() {
		// byteBuf.release();
		context.disconnect();
		context.close();
		return true;
	}

	@Override
	public String name() {
		return "NettySender-ContextHashCode:" + context.hashCode() + "&";// + byteBuf.capacity();
	}

	@Override
	public void send(byte[] msg) {
		logger.debug(microsecond() + " call sender send -> data length : " + msg.length);
		ByteBuf byteBuf = context.alloc().buffer(msg.length);
		byteBuf.writeBytes(msg);
		ChannelFuture writeAndFlush = context.writeAndFlush(byteBuf.retain());
		writeAndFlush.addListener(new ChannelFutureListener() {
			@Override
			public void operationComplete(ChannelFuture future) throws Exception {
				logger.debug(microsecond() + " call sender send operation complete -> data length : "
						+ byteBuf.writerIndex());
				byteBuf.clear();
				byteBuf.release();
			}
		});
	}

}