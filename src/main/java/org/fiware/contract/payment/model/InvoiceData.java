package org.fiware.contract.payment.model;

import lombok.Data;

import java.net.URL;

@Data
public class InvoiceData {

	private final String invoiceDataId;
	private final URL uploadUrl;
}
