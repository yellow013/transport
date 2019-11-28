package io.mercury.transport.core.config;

import io.mercury.common.config.Configurator;

public interface TransportConfigurator extends Configurator {

	public String getHost();

	public int getPort();

}
