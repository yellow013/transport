package io.mercury.transport.jeromq.config;

import io.mercury.transport.core.config.TransportConfigurator;

public class JeroMqConfigurator implements TransportConfigurator {

	private String host;
	private int port;
	private String topic;
	private int ioThreads;

	private final String configuratorName = "JeroMqConfigurator";

	private JeroMqConfigurator(Builder builder) {
		this.host = builder.host;
		this.port = builder.port;
		this.topic = builder.topic;
		this.ioThreads = builder.ioThreads;
	}

	public static Builder builder() {
		return new Builder();
	}

	@Override
	public String host() {
		return host;
	}

	@Override
	public int port() {
		return port;
	}

	public String topic() {
		return topic;
	}

	public int ioThreads() {
		return ioThreads;
	}

	@Override
	public String name() {
		return configuratorName;
	}

	public static class Builder {

		private String host;
		private int port;
		private String topic;
		private int ioThreads;

		private Builder() {
		}

		public Builder host(String host) {
			this.host = host;
			return this;
		}

		public Builder port(int port) {
			this.port = port;
			return this;
		}

		public Builder topic(String topic) {
			this.topic = topic;
			return this;
		}

		public Builder ioThreads(int ioThreads) {
			this.ioThreads = ioThreads;
			return this;
		}

		public JeroMqConfigurator build() {
			return new JeroMqConfigurator(this);
		}

	}
}