package io.mercury.transport.core.api;

import io.mercury.transport.core.TransportModule;

public interface Receiver extends TransportModule{
	
	void receive();
	
	void reconnect();

}
