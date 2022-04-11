package org.fiware.contract.model;

import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;

@Slf4j
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

	public static PaymentStatus getByValue(String value) {
		return Arrays.stream(values()).peek(v -> log.info("Value is {} - v: {}", value, v.value())).filter(v -> v.value().equals(value)).findFirst().orElseThrow(() -> new RuntimeException(String.format("No enum with value %s exists.", value)));
	}

}
