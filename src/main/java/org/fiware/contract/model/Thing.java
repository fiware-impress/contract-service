package org.fiware.contract.model;

import lombok.Data;

import java.net.URI;

/**
 * Parent type according to schema.org - {@see https://schema.org/Thing}
 */
@Data
public abstract class Thing {

	protected URI identifier;
}
