package io.ffreedom.transport.rabbitmq.config;

public abstract class BaseRabbitMqConfigurator {

	// 连接配置
	private ConnectionConfigurator connectionConfigurator;

	protected BaseRabbitMqConfigurator(ConnectionConfigurator connectionConfigurator) {
		this.connectionConfigurator = connectionConfigurator;
	}

	/**
	 * @return the connectionConfigurator
	 */
	public ConnectionConfigurator getConnectionConfigurator() {
		return connectionConfigurator;
	}

}
