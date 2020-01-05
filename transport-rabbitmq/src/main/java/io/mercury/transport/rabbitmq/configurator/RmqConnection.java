package io.mercury.transport.rabbitmq.configurator;

import javax.annotation.Nonnull;
import javax.net.ssl.SSLContext;

import io.mercury.common.functional.ShutdownEvent;
import io.mercury.common.util.Assertor;
import io.mercury.common.util.StringUtil;
import io.mercury.transport.core.config.TransportConfigurator;

public final class RmqConnection implements TransportConfigurator {

	/**
	 * 连接参数
	 */
	private String host;
	private int port;
	private String username;
	private String password;
	// 虚拟主机
	private String virtualHost;
	// SSL
	private SSLContext sslContext;
	// 连接超时时间
	private int connectionTimeout;
	// 自动恢复连接
	private boolean automaticRecovery;
	// 重试连接间隔
	private long recoveryInterval;
	// 握手通信超时时间
	private int handshakeTimeout;
	// 关闭超时时间
	private int shutdownTimeout;
	// 请求心跳超时时间
	private int requestedHeartbeat;
	// 停机处理回调函数
	private ShutdownEvent<Exception> shutdownEvent;

	// 配置器全名
	private String name;
	// 配置信息
	private String connectionInfo;

	private RmqConnection(Builder builder) {
		this.host = builder.host;
		this.port = builder.port;
		this.username = builder.username;
		this.password = builder.password;
		this.virtualHost = builder.virtualHost;
		this.sslContext = builder.sslContext;
		this.connectionTimeout = builder.connectionTimeout;
		this.automaticRecovery = builder.automaticRecovery;
		this.recoveryInterval = builder.recoveryInterval;
		this.handshakeTimeout = builder.handshakeTimeout;
		this.shutdownTimeout = builder.shutdownTimeout;
		this.requestedHeartbeat = builder.requestedHeartbeat;
		this.shutdownEvent = builder.shutdownEvent;
		this.connectionInfo = newConnectionInfo();
		this.name = newConfiguratorName();
	}

	private String newConnectionInfo() {
		return host + ":" + port + (virtualHost.equals("/") ? virtualHost : "/" + virtualHost);
	}

	private String newConfiguratorName() {
		return username + "@" + connectionInfo;
	}

	public static Builder configuration(@Nonnull String host, int port, @Nonnull String username,
			@Nonnull String password) {
		return new Builder(host, port, username, password);
	}

	public static Builder configuration(@Nonnull String host, int port, @Nonnull String username,
			@Nonnull String password, String virtualHost) {
		return new Builder(host, port, username, password, virtualHost);
	}

	/**
	 * 
	 */
	@Override
	public String name() {
		return name;
	}

	@Override
	public String host() {
		return host;
	}

	@Override
	public int port() {
		return port;
	}

	/**
	 * @return the username
	 */
	public String username() {
		return username;
	}

	/**
	 * @return the password
	 */
	public String password() {
		return password;
	}

	/**
	 * @return the virtualHost
	 */
	public String virtualHost() {
		return virtualHost;
	}

	/**
	 * @return the sslContext
	 */
	public SSLContext sslContext() {
		return sslContext;
	}

	/**
	 * @return the connectionTimeout
	 */
	public int connectionTimeout() {
		return connectionTimeout;
	}

	/**
	 * @return the automaticRecovery
	 */
	public boolean automaticRecovery() {
		return automaticRecovery;
	}

	/**
	 * @return the recoveryInterval
	 */
	public long recoveryInterval() {
		return recoveryInterval;
	}

	/**
	 * @return the handshakeTimeout
	 */
	public int handshakeTimeout() {
		return handshakeTimeout;
	}

	/**
	 * @return the shutdownTimeout
	 */
	public int shutdownTimeout() {
		return shutdownTimeout;
	}

	/**
	 * @return the requestedHeartbeat
	 */
	public int requestedHeartbeat() {
		return requestedHeartbeat;
	}

	/**
	 * @return the shutdownEvent
	 */
	public ShutdownEvent<Exception> shutdownEvent() {
		return shutdownEvent;
	}

	/**
	 * @return the connectionName
	 */
	public String connectionInfo() {
		return connectionInfo;
	}

	private transient String toStringCache;

	@Override
	public String toString() {
		if (toStringCache == null)
			toStringCache = StringUtil.reflectionToString(this);
		return toStringCache;
	}

	public static class Builder {
		/**
		 * 连接参数
		 */
		private String host;
		private int port;
		private String username;
		private String password;
		// 虚拟主机
		private String virtualHost = "/";
		// SSL
		private SSLContext sslContext;
		// 连接超时时间
		private int connectionTimeout = 60 * 1000;
		// 自动恢复连接
		private boolean automaticRecovery = true;
		// 重试连接间隔
		private long recoveryInterval = 10 * 1000;
		// 握手通信超时时间
		private int handshakeTimeout = 10 * 1000;
		// 关闭超时时间
		private int shutdownTimeout = 10 * 1000;
		// 请求心跳超时时间
		private int requestedHeartbeat = 20;
		// 停机处理回调函数
		private ShutdownEvent<Exception> shutdownEvent;

		private Builder(String host, int port, String username, String password) {
			this.host = Assertor.nonNull(host, "host");
			this.port = Assertor.intGreaterThan(port, 0, "port");
			this.username = Assertor.nonNull(username, "username");
			this.password = Assertor.nonNull(password, "password");
		}

		private Builder(String host, int port, String username, String password, String virtualHost) {
			this.host = Assertor.nonNull(host, "host");
			this.port = Assertor.intGreaterThan(port, 0, "port");
			this.username = Assertor.nonNull(username, "username");
			this.password = Assertor.nonNull(password, "password");
			if (virtualHost != null && !virtualHost.equals(""))
				this.virtualHost = virtualHost;
		}

		public RmqConnection build() {
			return new RmqConnection(this);
		}

		/**
		 * @param sslContext the sslContext to set
		 */
		public Builder sslContext(SSLContext sslContext) {
			this.sslContext = sslContext;
			return this;
		}

		/**
		 * @param connectionTimeout the connectionTimeout to set
		 */
		public Builder connectionTimeout(int connectionTimeout) {
			this.connectionTimeout = connectionTimeout;
			return this;
		}

		/**
		 * @param automaticRecovery the automaticRecovery to set
		 */
		public Builder automaticRecovery(boolean automaticRecovery) {
			this.automaticRecovery = automaticRecovery;
			return this;
		}

		/**
		 * @param recoveryInterval the recoveryInterval to set
		 */
		public Builder recoveryInterval(long recoveryInterval) {
			this.recoveryInterval = recoveryInterval;
			return this;
		}

		/**
		 * @param handshakeTimeout the handshakeTimeout to set
		 */
		public Builder handshakeTimeout(int handshakeTimeout) {
			this.handshakeTimeout = handshakeTimeout;
			return this;
		}

		/**
		 * @param shutdownTimeout the shutdownTimeout to set
		 */
		public Builder shutdownTimeout(int shutdownTimeout) {
			this.shutdownTimeout = shutdownTimeout;
			return this;
		}

		/**
		 * @param requestedHeartbeat the requestedHeartbeat to set
		 */
		public Builder requestedHeartbeat(int requestedHeartbeat) {
			this.requestedHeartbeat = requestedHeartbeat;
			return this;
		}

		/**
		 * @param shutdownEvent the shutdownEvent to set
		 */
		public Builder shutdownEvent(ShutdownEvent<Exception> shutdownEvent) {
			this.shutdownEvent = shutdownEvent;
			return this;
		}

	}

	public static void main(String[] args) {

		RmqConnection configuration = configuration("localhost", 5672, "admin", "admin", "report").build();
		System.out.println(configuration);
		System.out.println(configuration.name());

	}

}
