package io.mercury.transport.netty;

import static io.mercury.common.sys.CurrentRuntime.availableCores;

import org.slf4j.Logger;

import io.mercury.common.annotation.lang.ProtectedAbstractMethod;
import io.mercury.common.log.CommonLoggerFactory;
import io.mercury.transport.netty.configurator.NettyConfigurator;
import io.netty.channel.ChannelHandler;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;

public abstract class NettyTransport {

	protected String tag;
	protected NettyConfigurator configurator;

	protected EventLoopGroup workerGroup;

	protected Logger log = CommonLoggerFactory.getLogger(getClass());

	protected ChannelHandler[] channelHandlers;

	public NettyTransport(String tag, NettyConfigurator configurator, ChannelHandler... channelHandlers) {
		if (configurator == null) {
			throw new IllegalArgumentException("Param-configurator is null in NettyTransport constructor.");
		}
		if (channelHandlers == null) {
			throw new IllegalArgumentException("Param-channelHandlers is null in NettyTransport constructor.");
		}
		this.tag = tag;
		this.configurator = configurator;
		this.channelHandlers = channelHandlers;
		this.workerGroup = new NioEventLoopGroup(availableCores() * 2 - availableCores());
		init();
	}

	@ProtectedAbstractMethod
	protected abstract void init();

	public String name() {
		return tag;
	}

}
