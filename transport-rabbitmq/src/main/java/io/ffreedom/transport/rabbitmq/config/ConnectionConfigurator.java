package io.ffreedom.transport.rabbitmq.config;

import io.ffreedom.common.functional.ShutdownEvent;
import io.ffreedom.transport.core.config.TransportConfigurator;

import javax.net.ssl.SSLContext;

public abstract class ConnectionConfigurator implements TransportConfigurator {

	/**
	 * 连接参数
	 */
	protected String host;
	protected int port;
	protected String username;
	protected String password;
	// SSL
	protected SSLContext sslContext;
	// 连接超时时间
	protected int connectionTimeout = 60 * 1000;

	protected String virtualHost = "/";
	/**
	 * 队列定义参数
	 */
	// 是否持久化
	protected boolean durable = true;
	// 连接独占此队列
	protected boolean exclusive = false;
	// channel关闭后自动删除队列
	protected boolean autoDelete = false;
	// 自动恢复连接
	protected boolean automaticRecovery = true;
	// 重试连接间隔
	protected long recoveryInterval = 10 * 1000;
	// 握手通信超时时间
	protected int handshakeTimeout = 10 * 1000;
	// 关闭超时时间
	protected int shutdownTimeout = 10 * 1000;
	// 请求心跳超时时间
	protected int requestedHeartbeat = 20;
	// 停机处理回调函数
	protected ShutdownEvent<Exception> shutdownEvent;

	protected String configuratorName;

	protected ConnectionConfigurator(String configuratorName, String host, int port) {
		this.configuratorName = configuratorName;
		this.host = host;
		this.port = port;
	}

	protected ConnectionConfigurator(String configuratorName, String host, int port, String username, String password) {
		this.configuratorName = configuratorName;
		this.host = host;
		this.port = port;
		this.username = username;
		this.password = password;
	}

	public String getHost() {
		return host;
	}

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

	public boolean isDurable() {
		return durable;
	}

	public boolean isExclusive() {
		return exclusive;
	}

	public boolean isAutoDelete() {
		return autoDelete;
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

	public String getConfiguratorName() {
		return configuratorName;
	}

	public SSLContext getSslContext() {
		return sslContext;
	}
}
