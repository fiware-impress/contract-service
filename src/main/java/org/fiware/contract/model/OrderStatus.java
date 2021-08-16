package org.fiware.contract.model;

public enum OrderStatus {

	ORDER_CANCELLED("OrderCancelled"),
	ORDER_DELIVERED("OrderDelivered"),
	ORDER_IN_TRANSIT("OrderInTransit"),
	ORDER_PAYMENT_DUE("OrderPaymentDue"),
	ORDER_PICKUP_AVAILABLE("OrderPickupAvailable"),
	ORDER_PROBLEM("OrderProblem"),
	ORDER_PROCESSING("OrderProcessing"),
	ORDER_RETURNED("OrderReturned");
	
	private final String value;

	OrderStatus(String value) {
		this.value = value;
	}

	public String value() {
		return value;
	}

}
