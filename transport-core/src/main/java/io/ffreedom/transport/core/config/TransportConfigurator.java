package io.ffreedom.transport.core.config;

import io.ffreedom.common.config.Configurator;

public interface TransportConfigurator extends Configurator {

	public String getHost();

	public int getPort();

}
