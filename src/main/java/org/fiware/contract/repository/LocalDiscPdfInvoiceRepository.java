package org.fiware.contract.repository;

import io.micronaut.context.annotation.Requires;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fiware.contract.configuration.LocalDiscProperties;

import javax.inject.Singleton;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

/**
 * Repository implementation using the local disc as a backend.
 */
@Slf4j
@Singleton
@Requires(property = "local.enabled", value = "true")
@RequiredArgsConstructor
public class LocalDiscPdfInvoiceRepository implements PdfInvoiceRepository {

	private static final String PDF_FILENAME_TEMPLATE = "%s/%s";

	private final LocalDiscProperties localDiscProperties;

	@Override
	public void storePdfInvoice(String invoiceId, byte[] pdfFile) {
		Path filePath = getFilePath(invoiceId);
		if (Files.exists(filePath)) {
			throw new RuntimeException(String.format("Invoice file with id %s already exists.", invoiceId));
		}
		try (FileOutputStream stream = new FileOutputStream(filePath.toString())) {
			stream.write(pdfFile);
		} catch (IOException e) {
			throw new RuntimeException(String.format("Was not able to store file with id %s.", invoiceId), e);
		}
	}

	@Override
	public Optional<byte[]> getPdfInvoice(String invoiceId) {
		Path filePath = getFilePath(invoiceId);

		if (!Files.exists(filePath)) {
			log.info("No invoice {} exits", invoiceId);
			return Optional.empty();
		}
		try {
			return Optional.of(Files.readAllBytes(getFilePath(invoiceId)));
		} catch (IOException e) {
			throw new RuntimeException(String.format("Was not able to get invoice file %s.", invoiceId));
		}
	}

	/**
	 * Get the path of the pdf with the given id on the local disc
	 *
	 * @param id - id of the pdf to be retrieved
	 * @return the local path
	 */
	private Path getFilePath(String id) {
		return Path.of(String.format(PDF_FILENAME_TEMPLATE, localDiscProperties.getPdfFolder(), id));
	}
}
