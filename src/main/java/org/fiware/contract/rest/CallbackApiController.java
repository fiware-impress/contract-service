package org.fiware.contract.rest;

import io.micronaut.http.annotation.Controller;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fiware.contract.IdHelper;
import org.fiware.contract.api.PerseoCallbackApi;
import org.fiware.contract.model.CallbackInformationVO;
import org.fiware.contract.model.Invoice;
import org.fiware.contract.model.MonetaryAmount;
import org.fiware.contract.model.Order;
import org.fiware.contract.model.PaymentMethod;
import org.fiware.contract.model.PaymentStatus;
import org.fiware.contract.model.PriceDefinition;
import org.fiware.contract.repository.InvoiceRepository;
import org.fiware.contract.repository.OrderRepository;

import java.net.URI;
import java.time.Clock;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalUnit;
import java.util.List;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Controller
public class CallbackApiController implements PerseoCallbackApi {

	private final OrderRepository orderRepository;
	private final InvoiceRepository invoiceRepository;
	private final Clock clock;


	@Override
	public void handleCallback(CallbackInformationVO callbackInformationVO) {
		URI orderId = IdHelper.getUriFromId("order", callbackInformationVO.orderId());
		Order order = orderRepository.getOrderById(orderId).orElseThrow(() -> new RuntimeException(String.format("Was not able to find order with id %s", orderId)));
		PriceDefinition priceDefinition = order.getAcceptedOffer()
				.getItemOffered()
				.getPriceDefinitions()
				.stream()
				.filter(pd -> pd.getIdentifier().toString().equals(callbackInformationVO.getPriceDefinitionId()))
				.findAny().orElseThrow(() -> new RuntimeException((String.format("Was not able to find price definition %s for order %s.", callbackInformationVO.getPriceDefinitionId(), orderId))));
		Invoice invoice = new Invoice();
		invoice.setIdentifier(IdHelper.getUriFromId("invoice", UUID.randomUUID().toString()));
		invoice.setAccountId(order.getCustomer().getIdentifier().toString());
		invoice.setConfirmationNumber(order.getConfirmationNumber());
		invoice.setPaymentDueDate(clock.instant().plus(Duration.of(7, ChronoUnit.DAYS)));
		invoice.setPaymentMethod(PaymentMethod.BY_INVOICE);
		invoice.setPaymentStatus(PaymentStatus.PAYMENT_DUE);
		invoice.setProducer(order.getSeller());
		invoice.setCustomer(order.getCustomer());
		invoice.setReferencesOrder(List.of(order));
		MonetaryAmount monetaryAmount = new MonetaryAmount();
		monetaryAmount.setCurrency(priceDefinition.getPriceCurrency());
		monetaryAmount.setValue(priceDefinition.getPrice());
		invoice.setTotalPaymentDue(monetaryAmount);

		invoiceRepository.createInvoice(invoice);
		log.info("Received callback: {}", callbackInformationVO);
	}
}
