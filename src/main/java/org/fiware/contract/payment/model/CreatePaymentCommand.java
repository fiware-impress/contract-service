package org.fiware.contract.payment.model;

import lombok.Data;

@Data
public class CreatePaymentCommand extends Command {

	private final String command = "createPayment";
	private final WithAuthorizationProperties properties = new WithAuthorizationProperties(false);
	private final CreatePaymentData data;
}
