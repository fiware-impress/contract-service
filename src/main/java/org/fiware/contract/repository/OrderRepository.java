package org.fiware.contract.repository;

import io.micronaut.http.HttpResponse;
import org.fiware.broker.api.EntitiesApiClient;
import org.fiware.broker.model.EntityListVO;
import org.fiware.broker.model.EntityVO;
import org.fiware.contract.IdHelper;
import org.fiware.contract.configuration.GeneralProperties;
import org.fiware.contract.mapping.EntityMapper;
import org.fiware.contract.model.Order;

import javax.inject.Singleton;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Singleton
public class OrderRepository extends BrokerBaseRepository {

	private final OrganizationRepository organizationRepository;
	private final OfferRepository offerRepository;

	public OrderRepository(GeneralProperties generalProperties, EntityMapper entityMapper, EntitiesApiClient entitiesApi, OrganizationRepository organizationRepository, OfferRepository offerRepository) {
		super(generalProperties, entityMapper, entitiesApi);
		this.organizationRepository = organizationRepository;
		this.offerRepository = offerRepository;
	}

	public Optional<Order> getOrderById(URI id) {

		return entitiesApi
				.retrieveEntityById(id, null, null, null, getLinkHeader())
				.getBody()
				.map(entityVO -> entityMapper.entityVoToOrder(entityVO, organizationRepository, offerRepository));
	}

	public URI createOrder(Order order) {
		EntityVO orderEntityVo = entityMapper.orderToEntityVo(order, generalProperties.getContextUrl());
		HttpResponse<Object> response = entitiesApi.createEntity(orderEntityVo);
		return URI.create(IdHelper.getIdFromIdentifier(URI.create(response.getHeaders().get("Location"))));
	}

	public List<Order> getOrders() {
		List<Order> orderList = new ArrayList<>();
		Optional<EntityListVO> optionalEntityVOS = entitiesApi.queryEntities(null, null, "Order", null, null, null, null, null, null, null, null, null, getLinkHeader()).getBody();
		for (EntityVO entityVO : optionalEntityVOS.get()) {
			orderList.add(entityMapper.entityVoToOrder(entityVO, organizationRepository, offerRepository));
		}
		return orderList;
	}
}
