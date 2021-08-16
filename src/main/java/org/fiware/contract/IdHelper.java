package org.fiware.contract;

import java.net.URI;

public class IdHelper {


	private static final String ID_TEMPLATE = "urn:ngsi-ld:%s:%s";

	public static URI getUriFromId(String type, String id) {
		return URI.create(String.format(ID_TEMPLATE, type, id));
	}

	public static String getIdFromIdentifier(URI identifier) {
		String[] idComponents = identifier.toString().split(":");
		return idComponents[idComponents.length - 1];
	}

}
