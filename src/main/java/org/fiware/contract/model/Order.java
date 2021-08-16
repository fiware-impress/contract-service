package org.fiware.contract.model;

import lombok.Data;

@Data
public class Order extends Thing {

	private Offer acceptedOffer;
	private PostalAddress billingAddress;
	private String confirmationNumber;
	private Organization customer;
	private Organization seller;
	private Number discount;
	private String discountCurrency;
	private String orderNumber;
	private Invoice partOfInvoice;
	private PaymentMethod paymentMethod;

}
