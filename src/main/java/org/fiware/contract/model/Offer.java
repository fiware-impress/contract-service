package org.fiware.contract.model;

import lombok.Data;

@Data
public class Offer extends Thing {

	private PaymentMethod acceptedPaymentMethod;
	private String areaServed;
	private ItemAvailability availability;
	private String category;
	private Organization seller;
	private SmartService itemOffered;

}
