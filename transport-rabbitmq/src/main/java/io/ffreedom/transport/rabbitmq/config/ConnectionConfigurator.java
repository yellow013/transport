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
	private String virtualHost = "/";

	// 配置器全名
	private String configuratorName;

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

	protected ConnectionConfigurator(String host, int port, String username, String password) {
		this(host, port, username, password, "/");
	}

	protected ConnectionConfigurator(String host, int port, String username, String password, String virtualHost) {
		this.host = host;
		this.port = port;
		this.username = username;
		this.password = password;
		this.virtualHost = virtualHost;
		this.configuratorName = username + "@" + host + ":" + port
				+ (virtualHost.equals("/") ? virtualHost : "/" + virtualHost);
	}

	public static ConnectionConfigurator configuration(String host, int port, String username, String password) {
		return new ConnectionConfigurator(host, port, username, password);
	}

	public static ConnectionConfigurator configuration(String host, int port, String username, String password,
			String virtualHost) {
		return new ConnectionConfigurator(host, port, username, password, virtualHost);
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

	public ConnectionConfigurator setConfiguratorName(String configuratorName) {
		this.configuratorName = configuratorName;
		return this;
	}

	public ConnectionConfigurator setSslContext(SSLContext sslContext) {
		this.sslContext = sslContext;
		return this;
	}

	public ConnectionConfigurator setConnectionTimeout(int connectionTimeout) {
		this.connectionTimeout = connectionTimeout;
		return this;
	}

	public ConnectionConfigurator setAutomaticRecovery(boolean automaticRecovery) {
		this.automaticRecovery = automaticRecovery;
		return this;
	}

	public ConnectionConfigurator setRecoveryInterval(long recoveryInterval) {
		this.recoveryInterval = recoveryInterval;
		return this;
	}

	public ConnectionConfigurator setHandshakeTimeout(int handshakeTimeout) {
		this.handshakeTimeout = handshakeTimeout;
		return this;
	}

	public ConnectionConfigurator setShutdownTimeout(int shutdownTimeout) {
		this.shutdownTimeout = shutdownTimeout;
		return this;
	}

	public ConnectionConfigurator setRequestedHeartbeat(int requestedHeartbeat) {
		this.requestedHeartbeat = requestedHeartbeat;
		return this;
	}

	public ConnectionConfigurator setShutdownEvent(ShutdownEvent<Exception> shutdownEvent) {
		this.shutdownEvent = shutdownEvent;
		return this;
	}

	public static void main(String[] args) {

		ConnectionConfigurator configuration = configuration("localhost", 5672, "admin", "admin", "report");
		System.out.println(configuration.getConfiguratorName());

	}

}
