package io.ffreedom.transport.core;

public interface TransportModule {

	String getName();

	boolean isConnected();

	boolean destroy();

}
