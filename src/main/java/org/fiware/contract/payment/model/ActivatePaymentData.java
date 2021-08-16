package org.fiware.contract.payment.model;

import lombok.Data;

import java.net.URL;

@Data
public class ActivatePaymentData {

	private final String invoiceDataId;
	private final String legitimationKey;
	private final URL confirmationUrl;
}
