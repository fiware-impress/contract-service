package org.fiware.contract.model;

import lombok.Data;

import java.time.Instant;

@Data
public class BuildingCrane extends Thing {

	private String name;
	private Number radius;
	private Number hookHeight;
	private Number liftingCapacity;
	private Number liftingCapacityAtPeak;
	private String maintenanceInterval;
	private Instant lastMaintenance;
	private Instant nextMaintenance;
	private Number vibratoryRate;

}
