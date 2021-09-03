package org.fiware.contract.repository;

import io.micronaut.http.HttpResponse;
import lombok.extern.slf4j.Slf4j;
import org.fiware.broker.api.EntitiesApiClient;
import org.fiware.broker.model.EntityListVO;
import org.fiware.broker.model.EntityVO;
import org.fiware.contract.IdHelper;
import org.fiware.contract.configuration.GeneralProperties;
import org.fiware.contract.mapping.EntityMapper;
import org.fiware.contract.model.PriceDefinition;

import javax.inject.Singleton;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Singleton
public class PriceDefinitionRepository extends BrokerBaseRepository {

	private final MeasurementPointRepository measurementPointRepository;

	public PriceDefinitionRepository(GeneralProperties generalProperties, EntityMapper entityMapper, EntitiesApiClient entitiesApi, MeasurementPointRepository measurementPointRepository) {
		super(generalProperties, entityMapper, entitiesApi);
		this.measurementPointRepository = measurementPointRepository;
	}

	public PriceDefinition getPriceDefinitionById(URI id) {

		return entitiesApi
				.retrieveEntityById(generalProperties.getTenant(), id, null, null, null, getLinkHeader())
				.getBody()
				.map(entityVO -> entityMapper.entityVoToPriceDefinition(entityVO, measurementPointRepository))
				.orElseThrow(() -> new RuntimeException("No such pricedefinition exists."));
	}

	public URI createPriceDefinition(PriceDefinition priceDefinition) {
		EntityVO priceDefinitionEntityVO = entityMapper.priceDefinitionToEntityVo(priceDefinition, generalProperties.getContextUrl());
		HttpResponse<Object> response = entitiesApi.createEntity(generalProperties.getTenant(), priceDefinitionEntityVO);
		return URI.create(IdHelper.getIdFromIdentifier(URI.create(response.getHeaders().get("Location"))));
	}

	public List<PriceDefinition> getPriceDefinitions() {
		List<PriceDefinition> priceDefinitionList = new ArrayList<>();
		Optional<EntityListVO> optionalEntityVOS = entitiesApi.queryEntities(generalProperties.getTenant(), null, null, "PriceDefinition", null, null, null, null, null, null, null, null, null, getLinkHeader()).getBody();
		for (EntityVO entityVO : optionalEntityVOS.get()) {
			priceDefinitionList.add(entityMapper.entityVoToPriceDefinition(entityVO, measurementPointRepository));
		}
		return priceDefinitionList;
	}
}
