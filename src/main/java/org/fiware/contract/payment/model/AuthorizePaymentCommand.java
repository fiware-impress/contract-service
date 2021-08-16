package org.fiware.contract.payment.model;

import lombok.Data;

@Data
public class AuthorizePaymentCommand extends Command {

	private final String command = "authorizePayment";
	private final Object properties = null;
	private final AuthorizePaymentData data;
}
