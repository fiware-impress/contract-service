package org.fiware.contract;

import io.micronaut.http.HttpResponse;
import org.fiware.broker.api.EntitiesApiClient;
import org.fiware.broker.model.EntityFragmentVO;
import org.fiware.broker.model.EntityVO;
import org.fiware.contract.configuration.GeneralProperties;
import org.fiware.contract.mapping.EntityMapper;
import org.fiware.contract.model.Order;
import org.fiware.contract.repository.BrokerBaseRepository;

import javax.inject.Singleton;
import java.net.URI;

@Singleton
public class TestThingRepository extends BrokerBaseRepository {

	public TestThingRepository(GeneralProperties generalProperties, EntityMapper entityMapper, EntitiesApiClient entitiesApi) {
		super(generalProperties, entityMapper, entitiesApi);
	}


	public URI createThing(EntityVO entityVO) {
		HttpResponse<Object> response = entitiesApi.createEntity(generalProperties.getTenant(), entityVO);
		return URI.create(IdHelper.getIdFromIdentifier(URI.create(response.getHeaders().get("Location"))));
	}


	public void updateProperty(URI entityId, EntityFragmentVO fragmentVO) {
		entitiesApi.updateEntityAttrs(generalProperties.getTenant(), entityId, fragmentVO);
	}

}
