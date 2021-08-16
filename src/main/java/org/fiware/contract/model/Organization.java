package org.fiware.contract.model;

import lombok.Data;

@Data
public class Organization extends Thing {

	private String legalName;

	private ContactPoint contactPoint;
	private PostalAddress address;
}
