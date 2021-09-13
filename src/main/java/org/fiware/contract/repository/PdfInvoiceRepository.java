package org.fiware.contract.repository;

import java.util.Optional;

public interface PdfInvoiceRepository {

	void storePdfInvoice(String invoiceId, byte[] pdfFile);

	Optional<byte[]> getPdfInvoice(String invoiceId);
}
