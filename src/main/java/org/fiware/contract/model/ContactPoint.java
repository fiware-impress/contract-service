package org.fiware.contract.model;

import lombok.Data;

@Data
public class ContactPoint extends Thing {

	private String email;
	private String telephone;
	private String contactType;


}
