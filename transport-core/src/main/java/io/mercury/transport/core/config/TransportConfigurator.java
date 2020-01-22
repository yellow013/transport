package io.mercury.transport.core.config;

import io.mercury.common.config.Configurator;

public interface TransportConfigurator extends Configurator {

	public String host();

	public int port();
	
	public String connectionInfo();

}
