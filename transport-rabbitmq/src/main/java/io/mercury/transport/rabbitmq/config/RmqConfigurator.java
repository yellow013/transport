package io.mercury.transport.rabbitmq.config;

abstract class RmqConfigurator {

	// 连接配置
	private ConnectionConfigurator connectionConfigurator;

	protected RmqConfigurator(ConnectionConfigurator connectionConfigurator) {
		this.connectionConfigurator = connectionConfigurator;
	}

	/**
	 * @return the connectionConfigurator
	 */
	public ConnectionConfigurator getConnectionConfigurator() {
		return connectionConfigurator;
	}

}
