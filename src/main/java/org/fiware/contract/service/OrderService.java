package org.fiware.contract.service;

import io.micronaut.http.HttpResponse;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;
import lombok.RequiredArgsConstructor;
import org.fiware.broker.api.SubscriptionsApiClient;
import org.fiware.contract.model.ItemAvailability;
import org.fiware.contract.model.Offer;
import org.fiware.contract.model.Order;
import org.fiware.contract.repository.OfferRepository;
import org.fiware.contract.repository.OrderRepository;
import org.fiware.contract.repository.OrganizationRepository;
import org.fiware.contract.repository.PerseoRuleRepository;

import javax.inject.Singleton;
import java.net.URI;
import java.util.Optional;

@Singleton
@RequiredArgsConstructor
public class OrderService {

	private final OrderRepository orderRepository;
	private final OfferRepository offerRepository;
	private final PerseoRuleRepository ruleRepository;

	public Optional<URI> createOrder(Order order) {
		Offer acceptedOffer = offerRepository.getOffer(order.getAcceptedOffer().getIdentifier()).orElseThrow(() -> new RuntimeException("No such offer exists."));
		if(!acceptedOffer.getAvailability().equals(ItemAvailability.IN_STOCK)) {
			return Optional.empty();
		}
		URI orderURI = orderRepository.createOrder(order);
		createRulesForOrder(orderURI, order);
		acceptedOffer.setAvailability(ItemAvailability.OUT_OF_STOCK);
		offerRepository.updateOffer(acceptedOffer);
		return Optional.of(orderURI);
	}

	private void createRulesForOrder(URI orderURI, Order order) {
		order.getAcceptedOffer()
				.getItemOffered()
				.getPriceDefinitions()
				.forEach(priceDefinition -> {
					ruleRepository.createRule(
							orderURI.toString(),
							priceDefinition.getIdentifier().toString(),
							priceDefinition.getMeasurementPoint().getMeasurementQuery(),
							priceDefinition.getMeasurementPoint().getProvider().getIdentifier());
				});
	}
}
