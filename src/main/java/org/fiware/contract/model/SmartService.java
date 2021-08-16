package org.fiware.contract.model;

import lombok.Data;

import java.util.List;

@Data
public class SmartService extends Thing {

	private String category;
	private String serviceType;
	private List<PriceDefinition> priceDefinitions;
}
