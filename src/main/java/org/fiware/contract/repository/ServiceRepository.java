package org.fiware.contract.repository;

import io.micronaut.http.HttpResponse;
import org.fiware.broker.api.EntitiesApiClient;
import org.fiware.broker.model.EntityListVO;
import org.fiware.broker.model.EntityVO;
import org.fiware.contract.IdHelper;
import org.fiware.contract.configuration.GeneralProperties;
import org.fiware.contract.mapping.EntityMapper;
import org.fiware.contract.model.SmartService;

import javax.inject.Singleton;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Singleton
public class ServiceRepository extends BrokerBaseRepository {

	private final PriceDefinitionRepository priceDefinitionRepository;

	public ServiceRepository(GeneralProperties generalProperties, EntityMapper entityMapper, EntitiesApiClient entitiesApi, PriceDefinitionRepository priceDefinitionRepository) {
		super(generalProperties, entityMapper, entitiesApi);
		this.priceDefinitionRepository = priceDefinitionRepository;
	}

	public URI createService(SmartService smartService) {
		EntityVO serviceEntity = entityMapper.smartServiceToEntityVO(smartService, generalProperties.getContextUrl());
		HttpResponse<Object> response = entitiesApi.createEntity(generalProperties.getTenant(), serviceEntity);
		return URI.create(IdHelper.getIdFromIdentifier(URI.create(response.getHeaders().get("Location"))));
	}

	public List<SmartService> getServices() {
		List<SmartService> serviceList = new ArrayList<>();
		Optional<EntityListVO> optionalEntityVOS = entitiesApi.queryEntities(generalProperties.getTenant(), null, null, "SmartService", null, null, null, null, null, null, null, null, null, getLinkHeader()).getBody();
		for (EntityVO entityVO : optionalEntityVOS.get()) {
			serviceList.add(entityMapper.entityVoToSmartService(entityVO, priceDefinitionRepository));
		}
		return serviceList;
	}

	public Optional<SmartService> getService(URI serviceId) {
		return entitiesApi
				.retrieveEntityById(generalProperties.getTenant(), serviceId, null, null, null, getLinkHeader())
				.getBody()
				.map(entityVO -> entityMapper.entityVoToSmartService(entityVO, priceDefinitionRepository));
	}

}