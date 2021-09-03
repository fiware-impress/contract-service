package org.fiware.contract.model;

import lombok.Data;

import java.net.URI;
import java.util.UUID;

/**
 * Parent type according to schema.org - {@see https://schema.org/Thing}
 */
@Data
public class Thing {

	protected URI identifier;
}
