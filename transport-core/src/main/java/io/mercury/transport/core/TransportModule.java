package io.mercury.transport.core;

public interface TransportModule {

	String getName();

	boolean isConnected();

	boolean destroy();

}
