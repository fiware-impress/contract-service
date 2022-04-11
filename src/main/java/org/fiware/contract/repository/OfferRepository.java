package org.fiware.contract.repository;

import io.micronaut.http.HttpResponse;
import lombok.RequiredArgsConstructor;
import org.fiware.broker.api.EntitiesApiClient;
import org.fiware.broker.model.EntityFragmentVO;
import org.fiware.broker.model.EntityListVO;
import org.fiware.broker.model.EntityVO;
import org.fiware.contract.IdHelper;
import org.fiware.contract.configuration.GeneralProperties;
import org.fiware.contract.mapping.EntityMapper;
import org.fiware.contract.model.Offer;

import javax.inject.Singleton;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Singleton
public class OfferRepository extends BrokerBaseRepository {

	private final OrganizationRepository organizationRepository;
	private final ServiceRepository serviceRepository;

	public OfferRepository(GeneralProperties generalProperties, EntityMapper entityMapper, EntitiesApiClient entitiesApi, OrganizationRepository organizationRepository, ServiceRepository serviceRepository) {
		super(generalProperties, entityMapper, entitiesApi);
		this.organizationRepository = organizationRepository;
		this.serviceRepository = serviceRepository;
	}

	public URI createOffer(Offer offer) {
		EntityVO offerEntity = entityMapper.offerToEntityVO(generalProperties.getContextUrl(), offer);
		HttpResponse<Object> response = entitiesApi.createEntity(offerEntity, generalProperties.getTenant());
		return URI.create(IdHelper.getIdFromIdentifier(URI.create(response.getHeaders().get("Location"))));
	}

	public void updateOffer(Offer offer) {
		EntityFragmentVO offerEntityFragmentVO = entityMapper.entityToEntityFragmentVO(entityMapper.offerToEntityVO(generalProperties.getContextUrl(), offer));
		// ID an type cannot be updated
		offerEntityFragmentVO.setId(null);
		offerEntityFragmentVO.setType(null);
		entitiesApi.appendEntityAttrs(offer.getIdentifier(), offerEntityFragmentVO, generalProperties.getTenant(), null);
	}

	public List<Offer> getOffers() {
		List<Offer> offerList = new ArrayList<>();
		Optional<EntityListVO> optionalEntityVOS = entitiesApi.queryEntities(generalProperties.getTenant(), null, null, "Offer", null, null, null, null, null, null, null, null, null, getLinkHeader()).getBody();
		if (optionalEntityVOS.isEmpty()) {
			return List.of();
		}
		for (EntityVO entityVO : optionalEntityVOS.get()) {
			offerList.add(entityMapper.entityVoToOffer(entityVO, organizationRepository, serviceRepository));
		}
		return offerList;
	}

	public Optional<Offer> getOffer(URI offerId) {
		return entitiesApi
				.retrieveEntityById(offerId, generalProperties.getTenant(), null, null, null, getLinkHeader())
				.getBody()
				.map(entityVO -> entityMapper.entityVoToOffer(entityVO, organizationRepository, serviceRepository));
	}

}
