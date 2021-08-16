package org.fiware.contract.payment.model;

import lombok.Data;

@Data
public class ActivatePaymentCommand extends Command{

	private final String command = "activatePaymentGuard";
	private final Object properties = null;
	private final ActivatePaymentData data;

}
