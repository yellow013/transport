package io.ffreedom.transport.core.api;

import io.ffreedom.transport.core.TransportModule;

public interface Receiver extends TransportModule{
	
	void receive();
	
	void reconnect();

}
