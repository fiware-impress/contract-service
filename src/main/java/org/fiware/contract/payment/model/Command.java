package org.fiware.contract.payment.model;

public abstract class Command {

	public abstract String getCommand();
	public abstract Object getProperties();
	public abstract Object getData();
}
