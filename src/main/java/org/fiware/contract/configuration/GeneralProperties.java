package org.fiware.contract.configuration;

import io.micronaut.context.annotation.ConfigurationProperties;
import lombok.Data;

import java.net.URL;

/**
 * General properties to be used for the server.
 */
@ConfigurationProperties("general")
@Data
public class GeneralProperties {

	/**
	 * Base address of the context service. Will be used to create the upload url.
	 */
	private String serviceBaseAddress;

	/**
	 * ContextUrl for the service to use.
	 */
	private URL contextUrl;

	/**
	 * URL for the callback from perseo
	 */
	private URL contractServiceCallbackUrl;

	/**
	 * Tenant to be used by the contract server.
	 */
	private String tenant = null;
}
