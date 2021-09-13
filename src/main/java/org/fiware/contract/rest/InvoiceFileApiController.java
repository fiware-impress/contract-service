package org.fiware.contract.rest;

import io.micronaut.http.annotation.Controller;
import lombok.RequiredArgsConstructor;
import org.fiware.contract.api.InvoiceFilesApi;
import org.fiware.contract.repository.PdfInvoiceRepository;

import java.util.Optional;

@RequiredArgsConstructor
@Controller
public class InvoiceFileApiController implements InvoiceFilesApi {

	private final PdfInvoiceRepository pdfInvoiceRepository;

	@Override
	public Optional<byte[]> invoicePdfIdGet(String id) {
		return pdfInvoiceRepository.getPdfInvoice(id);
	}

	@Override
	public void invoicePdfIdPost(String id, byte[] body) {
		pdfInvoiceRepository.storePdfInvoice(id, body);
	}
}
