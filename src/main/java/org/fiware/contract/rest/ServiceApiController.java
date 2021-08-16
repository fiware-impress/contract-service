package org.fiware.contract.rest;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import lombok.RequiredArgsConstructor;
import org.fiware.contract.IdHelper;
import org.fiware.contract.api.SmartServiceApi;
import org.fiware.contract.mapping.EntityMapper;
import org.fiware.contract.model.SmartServiceVO;
import org.fiware.contract.repository.ServiceRepository;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Controller
public class ServiceApiController implements SmartServiceApi {

	private final ServiceRepository serviceRepository;
	private final EntityMapper entityMapper;

	@Override
	public HttpResponse<Object> createService(@NotNull SmartServiceVO smartServiceVO) {
		smartServiceVO.setId(UUID.randomUUID().toString());
		return HttpResponse.created(serviceRepository.createService(entityMapper.smartServiceVoToSmartService(smartServiceVO)));
	}

	@Override
	public Optional<SmartServiceVO> getServiceById(String id) {
		return serviceRepository.getService(IdHelper.getUriFromId("smart-service", id)).map(entityMapper::smartServiceToSmartServiceVo);
	}

	@Override
	public List<SmartServiceVO> getServices() {
		return serviceRepository.getServices().stream().map(entityMapper::smartServiceToSmartServiceVo).collect(Collectors.toList());
	}
}
