package org.fiware.contract.rest;

import io.micronaut.http.annotation.Controller;
import lombok.RequiredArgsConstructor;
import org.fiware.contract.api.InvoiceApi;
import org.fiware.contract.mapping.EntityMapper;
import org.fiware.contract.model.InvoiceVO;
import org.fiware.contract.repository.InvoiceRepository;
import org.fiware.contract.repository.PdfInvoiceRepository;

import java.net.URI;
import java.util.Optional;

@RequiredArgsConstructor
@Controller
public class InvoiceApiController implements InvoiceApi {

	private final PdfInvoiceRepository pdfInvoiceRepository;
	private final InvoiceRepository invoiceRepository;
	private final EntityMapper entityMapper;

	@Override
	public Optional<InvoiceVO> invoiceIdGet(String id) {
		return invoiceRepository.getInvoice(URI.create(id)).map(entityMapper::invoiceToInvoiceVO);
	}

	@Override
	public Optional<byte[]> invoiceIdPdfGet(String id) {
		return pdfInvoiceRepository.getPdfInvoice(id);
	}

	@Override
	public void invoiceIdPdfPost(String id, byte[] body) {
		pdfInvoiceRepository.storePdfInvoice(id, body);
	}
}
