package org.fiware.contract.model;

public enum ItemAvailability {

	BACK_ORDER("BackOrder"),
	DISCONTINUED("Discontinued"),
	IN_STOCK("InStock"),
	IN_STORE_ONLY("InStoreOnly"),
	LIMITED_AVAILABILITY("LimitedAvailability"),
	ONLINE_ONLY("OnlineOnly"),
	OUT_OF_STOCK("OutOfStock"),
	PRE_ORDER("PreOrder"),
	PRE_SALE("PreSale"),
	SOLD_OUT("SoldOut");

	private final String value;

	ItemAvailability(String value) {
		this.value = value;
	}

	public static ItemAvailability ofValue(String value) {
		for (ItemAvailability ia : values()) {
			if (ia.value().equals(value)) {
				return ia;
			}
		}
		throw new IllegalArgumentException(String.format("No such ItemAvailability exists: %s", value));
	}

	public String value() {
		return value;
	}

}
