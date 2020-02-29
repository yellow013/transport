package io.mercury.transport.core.api;

import io.mercury.transport.core.TransportModule;

public interface Receiver extends TransportModule{
	
	// Start receive
	void receive();
	
	void reconnect();

}
