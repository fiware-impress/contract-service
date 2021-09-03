package org.fiware.contract.model;

import lombok.Data;

@Data
public class Order extends Thing {

	private String confirmationNumber;
	private Number discount;
	private String discountCurrency;
	private String orderNumber;

	private PostalAddress billingAddress;

	private Offer acceptedOffer;

	private Organization customer;
	private Organization seller;

	private PaymentMethod paymentMethod;

}
