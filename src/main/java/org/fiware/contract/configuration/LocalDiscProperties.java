package org.fiware.contract.configuration;

import io.micronaut.context.annotation.ConfigurationProperties;
import lombok.Data;

/**
 * Use the local file system as storage backend
 */
@ConfigurationProperties("local")
@Data
public class LocalDiscProperties {

	/**
	 * Should local storage be enabled.
	 */
	private boolean enabled = true;
	/**
	 * The folder to store the pdfs at.
	 */
	private String pdfFolder = "/ld-contexts";

}
