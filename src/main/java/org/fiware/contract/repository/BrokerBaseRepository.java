package org.fiware.contract.repository;

import lombok.RequiredArgsConstructor;
import org.fiware.broker.api.EntitiesApiClient;
import org.fiware.contract.configuration.GeneralProperties;
import org.fiware.contract.mapping.EntityMapper;

@RequiredArgsConstructor
public abstract class BrokerBaseRepository {

	protected final GeneralProperties generalProperties;
	protected final EntityMapper entityMapper;
	protected final EntitiesApiClient entitiesApi;

	protected String getLinkHeader() {
		return String.format("<%s>; rel=\"http://www.w3.org/ns/json-ld#context\"; type=\"application/ld+json", generalProperties.getContextUrl());
	}
}
