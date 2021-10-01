package org.fiware.contract.repository;

import io.micronaut.http.HttpResponse;
import lombok.extern.slf4j.Slf4j;
import org.fiware.broker.api.EntitiesApiClient;
import org.fiware.broker.model.EntityListVO;
import org.fiware.broker.model.EntityVO;
import org.fiware.contract.IdHelper;
import org.fiware.contract.configuration.GeneralProperties;
import org.fiware.contract.mapping.EntityMapper;
import org.fiware.contract.model.MeasurementPoint;
import org.fiware.contract.model.Organization;

import javax.inject.Singleton;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Singleton
public class MeasurementPointRepository extends BrokerBaseRepository {

	private final ThingRepository thingRepository;

	public MeasurementPointRepository(GeneralProperties generalProperties, EntityMapper entityMapper, EntitiesApiClient entitiesApi, ThingRepository thingRepository) {
		super(generalProperties, entityMapper, entitiesApi);
		this.thingRepository = thingRepository;
	}


	public MeasurementPoint getMeasurementPointById(URI id) {

		return entitiesApi
				.retrieveEntityById(generalProperties.getTenant(), id, null, null, null, getLinkHeader())
				.getBody()
				.map(entityVO -> entityMapper.entityVoToMeasurementPoint(entityVO, thingRepository))
				.orElseThrow(() -> new RuntimeException("No such measurementpoint exists."));
	}

	public URI createMeasurementPoint(MeasurementPoint measurementPoint) {
		EntityVO measurementPointEntityVO = entityMapper.measurementPointToEntityVO(measurementPoint, generalProperties.getContextUrl());
		HttpResponse<Object> response = entitiesApi.createEntity(generalProperties.getTenant(), measurementPointEntityVO);
		return URI.create(IdHelper.getIdFromIdentifier(URI.create(response.getHeaders().get("Location"))));
	}

	public List<MeasurementPoint> getMeasurementPoints() {
		List<MeasurementPoint> measurementPointList = new ArrayList<>();
		Optional<EntityListVO> optionalEntityVOS = entitiesApi.queryEntities(generalProperties.getTenant(), null, null, "MeasurementPoint", null, null, null, null, null, null, null, null, null, getLinkHeader()).getBody();
		if (optionalEntityVOS.isEmpty()) {
			return List.of();
		}
		for (EntityVO entityVO : optionalEntityVOS.get()) {
			measurementPointList.add(entityMapper.entityVoToMeasurementPoint(entityVO, thingRepository));
		}
		return measurementPointList;
	}
}
