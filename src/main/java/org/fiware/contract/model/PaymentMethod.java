package org.fiware.contract.model;

public enum PaymentMethod {

	BY_BANK_TRANSFER_IN_ADVANCE("ByBankTransferInAdvance"),
	BY_INVOICE("ByInvoice"),
	CASH("Cash"),
	CHECK_IN_ADVANCE("CheckInAdvance"),
	COD("COD"),
	DIRECT_DEBIT("DirectDebit"),
	GOOGLE_CHECKOUT("GoogleCheckout"),
	PAY_PAL("PayPal"),
	PAY_SWARM("PaySwarm");

	private final String value;

	PaymentMethod(String value) {
		this.value = value;
	}

	public String value() {
		return value;
	}
}
