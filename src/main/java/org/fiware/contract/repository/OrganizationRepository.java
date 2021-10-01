package org.fiware.contract.repository;

import io.micronaut.http.HttpResponse;
import org.fiware.broker.api.EntitiesApiClient;
import org.fiware.broker.model.EntityListVO;
import org.fiware.broker.model.EntityVO;
import org.fiware.contract.IdHelper;
import org.fiware.contract.configuration.GeneralProperties;
import org.fiware.contract.mapping.EntityMapper;
import org.fiware.contract.model.Organization;

import javax.inject.Singleton;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Singleton
public class OrganizationRepository extends BrokerBaseRepository {

	public OrganizationRepository(GeneralProperties generalProperties, EntityMapper entityMapper, EntitiesApiClient entitiesApi, EntitiesApiClient entitiesApi1) {
		super(generalProperties, entityMapper, entitiesApi);
	}

	public Organization getOrganizationById(URI id) {

		return entitiesApi
				.retrieveEntityById(generalProperties.getTenant(), id, null, null, null, getLinkHeader())
				.getBody()
				.map(entityMapper::entityVoToOrganization)
				.orElseThrow(() -> new RuntimeException("No such organization exists."));
	}

	public URI createOrganization(Organization organization) {
		EntityVO organizationEntityVO = entityMapper.organizationToEntityVO(generalProperties.getContextUrl(), organization);
		HttpResponse<Object> response = entitiesApi.createEntity(generalProperties.getTenant(), organizationEntityVO);
		return URI.create(IdHelper.getIdFromIdentifier(URI.create(response.getHeaders().get("Location"))));
	}

	public List<Organization> getOrganizations() {
		List<Organization> organizationList = new ArrayList<>();
		Optional<EntityListVO> optionalEntityVOS = entitiesApi.queryEntities(generalProperties.getTenant(), null, null, "Organization", null, null, null, null, null, null, null, null, null, getLinkHeader()).getBody();
		if (optionalEntityVOS.isEmpty()) {
			return List.of();
		}
		for (EntityVO entityVO : optionalEntityVOS.get()) {
			organizationList.add(entityMapper.entityVoToOrganization(entityVO));
		}
		return organizationList;
	}
}
