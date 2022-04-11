package org.fiware.contract.repository;

import org.fiware.broker.api.EntitiesApiClient;
import org.fiware.contract.configuration.GeneralProperties;
import org.fiware.contract.mapping.EntityMapper;
import org.fiware.contract.model.Thing;

import javax.inject.Singleton;
import java.net.URI;
import java.util.Optional;

@Singleton
public class ThingRepository extends BrokerBaseRepository {


	public ThingRepository(GeneralProperties generalProperties, EntityMapper entityMapper, EntitiesApiClient entitiesApi) {
		super(generalProperties, entityMapper, entitiesApi);
	}

	public Optional<Thing> getThingById(URI thingId) {
		return entitiesApi
				.retrieveEntityById(thingId, generalProperties.getTenant(), null, null, null, getLinkHeader())
				.getBody()
				.map(entityVO -> entityMapper.entityVoToThing(entityVO));
	}

}
