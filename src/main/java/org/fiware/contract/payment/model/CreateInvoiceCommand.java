package org.fiware.contract.payment.model;

import lombok.Data;

@Data
public class CreateInvoiceCommand extends Command {

	private final String command = "createInvoice";
	private final Object properties = new PaymentGuardProperties(true);
	private final InvoiceData data;
}
