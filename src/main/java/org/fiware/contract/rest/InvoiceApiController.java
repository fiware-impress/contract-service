package org.fiware.contract.rest;

import io.micronaut.http.annotation.Controller;
import lombok.RequiredArgsConstructor;
import org.fiware.contract.api.InvoiceApi;
import org.fiware.contract.mapping.EntityMapper;
import org.fiware.contract.model.InvoiceVO;
import org.fiware.contract.repository.InvoiceRepository;
import org.fiware.contract.repository.PdfInvoiceRepository;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Controller
public class InvoiceApiController implements InvoiceApi {

	private final PdfInvoiceRepository pdfInvoiceRepository;
	private final InvoiceRepository invoiceRepository;
	private final EntityMapper entityMapper;

	@Override
	public Optional<InvoiceVO> getInvoiceById(String id) {
		return invoiceRepository.getInvoice(URI.create(id)).map(entityMapper::invoiceToInvoiceVO);
	}

	@Override
	public Optional<List<InvoiceVO>> getInvoices() {
		return Optional.of(invoiceRepository.getInvoices().stream().map(entityMapper::invoiceToInvoiceVO).collect(Collectors.toList()));

	}

	@Override
	public Optional<byte[]> retrieveInvoicePdf(String id) {
		return pdfInvoiceRepository.getPdfInvoice(id);
	}

	@Override
	public void uploadInvoicePdf(String id, byte[] body) {
		pdfInvoiceRepository.storePdfInvoice(id, body);
	}

}
