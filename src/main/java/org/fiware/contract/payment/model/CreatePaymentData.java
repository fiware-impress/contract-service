package org.fiware.contract.payment.model;

import lombok.Data;

import java.net.URL;

@Data
public class CreatePaymentData {

	private final String paymentDataId;
	private final URL confirmationUrl;
}
