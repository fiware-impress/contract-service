package org.fiware.contract.rest;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import lombok.RequiredArgsConstructor;
import org.fiware.contract.IdHelper;
import org.fiware.contract.api.OrganizationApi;
import org.fiware.contract.mapping.EntityMapper;
import org.fiware.contract.model.Organization;
import org.fiware.contract.model.OrganizationVO;
import org.fiware.contract.repository.OrganizationRepository;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Controller
public class OrganizationApiController implements OrganizationApi {

	private final OrganizationRepository organizationRepository;
	private final EntityMapper entityMapper;

	@Override
	public HttpResponse<Object> createOrganization(@Valid @NotNull OrganizationVO organizationVO) {
		organizationVO.setId(UUID.randomUUID().toString());
		Organization organization = entityMapper.organizationVoToOrganization(organizationVO);

		return HttpResponse.created(organizationRepository.createOrganization(organization));
	}

	@Override
	public Optional<OrganizationVO> getOrganizationById(String id) {

		try {
			return Optional.of(entityMapper.organizationToOrganizationVo(organizationRepository.getOrganizationById(IdHelper.getUriFromId("organization", id))));
		} catch (RuntimeException e) {
			return Optional.empty();
		}
	}

	@Override
	public List<OrganizationVO> getOrganizations() {
		return organizationRepository.getOrganizations().stream().map(entityMapper::organizationToOrganizationVo).collect(Collectors.toList());
	}
}
