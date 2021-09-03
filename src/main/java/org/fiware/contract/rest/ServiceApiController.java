package org.fiware.contract.rest;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;
import lombok.RequiredArgsConstructor;
import org.fiware.contract.IdHelper;
import org.fiware.contract.api.SmartServiceApi;
import org.fiware.contract.mapping.EntityMapper;
import org.fiware.contract.model.MeasurementPoint;
import org.fiware.contract.model.PriceDefinition;
import org.fiware.contract.model.SmartService;
import org.fiware.contract.model.SmartServiceVO;
import org.fiware.contract.repository.MeasurementPointRepository;
import org.fiware.contract.repository.PriceDefinitionRepository;
import org.fiware.contract.repository.ServiceRepository;
import org.fiware.contract.repository.ThingRepository;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Controller
public class ServiceApiController implements SmartServiceApi {

	private final ThingRepository thingRepository;
	private final PriceDefinitionRepository priceDefinitionRepository;
	private final MeasurementPointRepository measurementPointRepository;
	private final ServiceRepository serviceRepository;
	private final EntityMapper entityMapper;

	// required, since the mutliple calls to orion and  can block the event loop
	@ExecuteOn(TaskExecutors.IO)
	@Override
	public HttpResponse<Object> createService(@NotNull SmartServiceVO smartServiceVO) {
		smartServiceVO.setId(UUID.randomUUID().toString());
		SmartService smartService = entityMapper.smartServiceVoToSmartService(smartServiceVO, thingRepository);
		List<PriceDefinition> persistedPriceDefinitions = smartService.getPriceDefinitions()
				.stream()
				.peek(priceDefinition -> priceDefinition.setIdentifier(persistPriceDefinition(priceDefinition)))
				.collect(Collectors.toList());
		smartService.setPriceDefinitions(persistedPriceDefinitions);
		return HttpResponse.created(serviceRepository.createService(smartService));
	}

	private URI persistPriceDefinition(PriceDefinition priceDefinition) {
		URI priceDefinitionId = IdHelper.getUriFromId("price-definition", UUID.randomUUID().toString());
		priceDefinition.setIdentifier(priceDefinitionId);
		URI measurementPointId = IdHelper.getUriFromId("measurement-point", UUID.randomUUID().toString());
		priceDefinition.getMeasurementPoint().setIdentifier(measurementPointId);
		measurementPointRepository.createMeasurementPoint(priceDefinition.getMeasurementPoint());
		priceDefinitionRepository.createPriceDefinition(priceDefinition);
		return priceDefinitionId;
	}

	// required, since the mutliple calls to orion and  can block the event loop
	@ExecuteOn(TaskExecutors.IO)
	@Override
	public Optional<SmartServiceVO> getServiceById(String id) {
		return serviceRepository.getService(IdHelper.getUriFromId("smart-service", id)).map(entityMapper::smartServiceToSmartServiceVo);
	}

	// required, since the mutliple calls to orion and  can block the event loop
	@ExecuteOn(TaskExecutors.IO)
	@Override
	public List<SmartServiceVO> getServices() {
		return serviceRepository.getServices().stream().map(entityMapper::smartServiceToSmartServiceVo).collect(Collectors.toList());
	}
}
