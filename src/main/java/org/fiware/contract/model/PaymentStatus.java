package org.fiware.contract.model;

public enum PaymentStatus {

	PAYMENT_AUTOMATICALLY_APPLIED("PaymentAutomaticallyApplied"),
	PAYMENT_COMPLETE("PaymentComplete"),
	PAYMENT_DECLINED("PaymentDeclined"),
	PAYMENT_DUE("PaymentDue"),
	PAYMENT_PAST_DUE("PaymentPastDue");

	private final String value;

	PaymentStatus(String value) {
		this.value = value;
	}

	public String value() {
		return value;
	}

}
