package org.fiware.contract.payment;

import lombok.RequiredArgsConstructor;
import org.fiware.contract.payment.model.ActivatePaymentCommand;
import org.fiware.contract.payment.model.ActivatePaymentData;
import org.fiware.contract.payment.model.AuthorizePaymentCommand;
import org.fiware.contract.payment.model.AuthorizePaymentData;
import org.fiware.contract.payment.model.Command;
import org.fiware.contract.payment.model.CreateInvoiceCommand;
import org.fiware.contract.payment.model.CreatePaymentCommand;
import org.fiware.contract.payment.model.CreatePaymentData;
import org.fiware.contract.payment.model.InvoiceData;
import org.fiware.dn.api.DefaultApi;
import org.fiware.dn.model.CommandVO;

import javax.inject.Singleton;
import java.net.URL;

@RequiredArgsConstructor
@Singleton
public class PaymentService {

	private final DefaultApi defaultApi;

	public void activatePaymentGuard(ActivatePaymentData activatePaymentData) {
		defaultApi.postPayment(commandToCommandVO(new ActivatePaymentCommand(activatePaymentData)));
	}

	public void createInvoice(InvoiceData invoiceData) {
		defaultApi.postPayment(commandToCommandVO(new CreateInvoiceCommand(invoiceData)));
	}

	public void createPayment(CreatePaymentData createPaymentData) {
		defaultApi.postPayment(commandToCommandVO(new CreatePaymentCommand(createPaymentData)));
	}

	public void authorizePayment(AuthorizePaymentData authorizePaymentData) {
		defaultApi.postPayment(commandToCommandVO(new AuthorizePaymentCommand(authorizePaymentData)));
	}

	private CommandVO commandToCommandVO(Command command) {
		CommandVO commandVo = new CommandVO();
		commandVo.setCommand(command.getCommand());
		commandVo.setProperties(command.getProperties());
		commandVo.setData(command.getData());
		return commandVo;
	}
}
