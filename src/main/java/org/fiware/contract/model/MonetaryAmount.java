package org.fiware.contract.model;

import lombok.Data;

@Data
public class MonetaryAmount extends Thing {

	private String currency;
	private Number value;
}
