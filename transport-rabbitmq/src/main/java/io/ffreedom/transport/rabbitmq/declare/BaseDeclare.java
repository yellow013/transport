package io.ffreedom.transport.rabbitmq.declare;

import org.eclipse.collections.api.list.MutableList;

import io.ffreedom.common.collections.MutableLists;
import io.ffreedom.transport.rabbitmq.config.ConnectionConfigurator;
import io.ffreedom.transport.rabbitmq.declare.BaseEntity.Binding;

public abstract class BaseDeclare {

	protected MutableList<Binding> bindings = MutableLists.newFastList();

	public abstract void declare(ConnectionConfigurator configurator);

}
