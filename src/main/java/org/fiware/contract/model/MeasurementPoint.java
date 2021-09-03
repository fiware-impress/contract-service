package org.fiware.contract.model;

import lombok.Data;

@Data
public class MeasurementPoint extends Thing{

	private String unitCode;
	private Thing provider;
	// query to get the measurement. F.e. a perseo rule-query
	private String measurementQuery;

}
