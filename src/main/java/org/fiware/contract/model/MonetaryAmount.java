package org.fiware.contract.model;

import lombok.Data;

@Data
public class MonetaryAmount {

	private String currency;
	private Number value;
}
