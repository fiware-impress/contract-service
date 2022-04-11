package org.fiware.contract.payment;

import lombok.RequiredArgsConstructor;
import org.fiware.contract.payment.model.ActivatePaymentData;
import org.fiware.contract.payment.model.AuthorizePaymentData;
import org.fiware.contract.payment.model.CreatePaymentData;
import org.fiware.contract.payment.model.InvoiceData;
import org.fiware.dn.api.PaymentsApi;
import org.fiware.dn.model.InvoiceCommandVO;
import org.fiware.dn.model.PaymentCommandDataVO;
import org.fiware.dn.model.PaymentCommandPropertiesVO;
import org.fiware.dn.model.PaymentCommandVO;

import javax.inject.Singleton;

@RequiredArgsConstructor
@Singleton
public class PaymentService {

	private final PaymentsApi paymentsApi;

	public void activatePaymentGuard(ActivatePaymentData activatePaymentData) {

		PaymentCommandVO paymentCommandVO = new PaymentCommandVO();
		paymentCommandVO.setCommand("activatePaymentGuard");

		PaymentCommandDataVO paymentCommandDataVO = new PaymentCommandDataVO();
		paymentCommandDataVO.setObjectDataID(activatePaymentData.getInvoiceDataId());
		paymentCommandDataVO.setConfirmationURL(activatePaymentData.getConfirmationUrl().toString());
		paymentCommandDataVO.setLegitimationKey(activatePaymentData.getLegitimationKey());
		paymentCommandVO.setData(paymentCommandDataVO);

		paymentsApi.postPayment(paymentCommandVO);
	}

	public void createInvoice(InvoiceData invoiceData) {
		InvoiceCommandVO invoiceCommandVO = new InvoiceCommandVO();
		invoiceCommandVO.setCommand("createInvoice");

		PaymentCommandDataVO paymentCommandDataVO = new PaymentCommandDataVO();
		paymentCommandDataVO.setConfirmationURL(invoiceData.getUploadUrl().toString());
		paymentCommandDataVO.setObjectDataID(invoiceData.getInvoiceDataId());
		invoiceCommandVO.setData(paymentCommandDataVO);

		PaymentCommandPropertiesVO paymentCommandPropertiesVO = new PaymentCommandPropertiesVO();
		paymentCommandPropertiesVO.setWithAuthorization(true);
		invoiceCommandVO.setProperties(paymentCommandPropertiesVO);

		paymentsApi.postInvoice(invoiceCommandVO);
	}

	public void createPayment(CreatePaymentData createPaymentData) {
		PaymentCommandVO paymentCommandVO = new PaymentCommandVO();
		paymentCommandVO.setCommand("createPayment");

		PaymentCommandDataVO paymentCommandDataVO = new PaymentCommandDataVO();
		paymentCommandDataVO.setObjectDataID(createPaymentData.getPaymentDataId());
		paymentCommandDataVO.setConfirmationURL(createPaymentData.getConfirmationUrl().toString());
		paymentCommandVO.setData(paymentCommandDataVO);

		PaymentCommandPropertiesVO paymentCommandPropertiesVO = new PaymentCommandPropertiesVO();
		paymentCommandPropertiesVO.setWithAuthorization(false);
		paymentCommandVO.setProperties(paymentCommandPropertiesVO);

		paymentsApi.postPayment(paymentCommandVO);
	}

	public void authorizePayment(AuthorizePaymentData authorizePaymentData) {
		PaymentCommandVO paymentCommandVO =new PaymentCommandVO();
		paymentCommandVO.setCommand("authorizePayment");

		PaymentCommandDataVO paymentCommandDataVO = new PaymentCommandDataVO();
		paymentCommandDataVO.setObjectDataID(authorizePaymentData.getPaymentDataId());
		paymentCommandDataVO.setConfirmationURL(authorizePaymentData.getConfirmationURL().toString());
		paymentCommandDataVO.setLegitimationKey(authorizePaymentData.getLegitimationKey());
		paymentCommandVO.setData(paymentCommandDataVO);

		paymentsApi.postPayment(paymentCommandVO);
	}

}
