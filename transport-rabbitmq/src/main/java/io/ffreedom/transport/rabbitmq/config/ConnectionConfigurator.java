package io.ffreedom.transport.rabbitmq.config;

import io.ffreedom.common.functional.ShutdownEvent;
import io.ffreedom.transport.core.config.TransportConfigurator;

import javax.net.ssl.SSLContext;

public final class ConnectionConfigurator implements TransportConfigurator {

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
	private String configuratorName;

	private ConnectionConfigurator(Builder builder) {
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
		this.configuratorName = newConfiguratorName();
	}

	private String newConfiguratorName() {
		return username + "@" + host + ":" + port + (virtualHost.equals("/") ? virtualHost : "/" + virtualHost);
	}

	public static Builder configuration(String host, int port, String username, String password) {
		return new Builder(host, port, username, password);
	}

	public static Builder configuration(String host, int port, String username, String password, String virtualHost) {
		return new Builder(host, port, username, password, virtualHost);
	}

	/**
	 * 
	 */
	@Override
	public String getConfiguratorName() {
		return configuratorName;
	}

	@Override
	public String getHost() {
		return host;
	}

	@Override
	public int getPort() {
		return port;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public String getVirtualHost() {
		return virtualHost;
	}

	public int getConnectionTimeout() {
		return connectionTimeout;
	}

	public boolean isAutomaticRecovery() {
		return automaticRecovery;
	}

	public long getRecoveryInterval() {
		return recoveryInterval;
	}

	public int getHandshakeTimeout() {
		return handshakeTimeout;
	}

	public int getShutdownTimeout() {
		return shutdownTimeout;
	}

	public int getRequestedHeartbeat() {
		return requestedHeartbeat;
	}

	public ShutdownEvent<Exception> getShutdownEvent() {
		return shutdownEvent;
	}

	public SSLContext getSslContext() {
		return sslContext;
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
			super();
			this.host = host;
			this.port = port;
			this.username = username;
			this.password = password;
		}

		private Builder(String host, int port, String username, String password, String virtualHost) {
			super();
			this.host = host;
			this.port = port;
			this.username = username;
			this.password = password;
			if (virtualHost != null && !virtualHost.equals(""))
				this.virtualHost = virtualHost;
		}

		public ConnectionConfigurator build() {
			return new ConnectionConfigurator(this);
		}

		/**
		 * @param sslContext the sslContext to set
		 */
		public Builder setSslContext(SSLContext sslContext) {
			this.sslContext = sslContext;
			return this;
		}

		/**
		 * @param connectionTimeout the connectionTimeout to set
		 */
		public Builder setConnectionTimeout(int connectionTimeout) {
			this.connectionTimeout = connectionTimeout;
			return this;
		}

		/**
		 * @param automaticRecovery the automaticRecovery to set
		 */
		public Builder setAutomaticRecovery(boolean automaticRecovery) {
			this.automaticRecovery = automaticRecovery;
			return this;
		}

		/**
		 * @param recoveryInterval the recoveryInterval to set
		 */
		public Builder setRecoveryInterval(long recoveryInterval) {
			this.recoveryInterval = recoveryInterval;
			return this;
		}

		/**
		 * @param handshakeTimeout the handshakeTimeout to set
		 */
		public Builder setHandshakeTimeout(int handshakeTimeout) {
			this.handshakeTimeout = handshakeTimeout;
			return this;
		}

		/**
		 * @param shutdownTimeout the shutdownTimeout to set
		 */
		public Builder setShutdownTimeout(int shutdownTimeout) {
			this.shutdownTimeout = shutdownTimeout;
			return this;
		}

		/**
		 * @param requestedHeartbeat the requestedHeartbeat to set
		 */
		public Builder setRequestedHeartbeat(int requestedHeartbeat) {
			this.requestedHeartbeat = requestedHeartbeat;
			return this;
		}

		/**
		 * @param shutdownEvent the shutdownEvent to set
		 */
		public Builder setShutdownEvent(ShutdownEvent<Exception> shutdownEvent) {
			this.shutdownEvent = shutdownEvent;
			return this;
		}

	}

	public static void main(String[] args) {

		ConnectionConfigurator configuration = configuration("localhost", 5672, "admin", "admin", "report").build();
		System.out.println(configuration.getConfiguratorName());

	}

}
