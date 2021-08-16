package org.fiware.contract.model;

import lombok.Data;

@Data
public class MeasurementPoint {

	private String unitCode;
	private GeneralThing provider;
	// property of the provider that serves the measurement.
	private String property;

}
