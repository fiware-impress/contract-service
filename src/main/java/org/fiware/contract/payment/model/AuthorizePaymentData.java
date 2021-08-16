package org.fiware.contract.payment.model;

import lombok.Data;

import java.net.URL;

@Data
public class AuthorizePaymentData {

	private final String paymentDataId;
	private final String legitimationKey;
	private final URL confirmationURL;
}
