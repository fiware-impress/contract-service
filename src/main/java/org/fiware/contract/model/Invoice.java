package org.fiware.contract.model;

import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
public class Invoice extends Thing {

	private String accountId;
	private String confirmationNumber;

	private Instant paymentDueDate;
	private Instant creationDate;
	private PaymentMethod paymentMethod;
	private String paymentMethodId;

	private PaymentStatus paymentStatus;

	private Organization producer;
	private Organization customer;

	private List<Order> referencesOrder;

	private MonetaryAmount totalPaymentDue;
}
