package org.fiware.contract.rest;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import lombok.RequiredArgsConstructor;
import org.fiware.contract.IdHelper;
import org.fiware.contract.api.OfferApi;
import org.fiware.contract.mapping.EntityMapper;
import org.fiware.contract.model.Offer;
import org.fiware.contract.model.OfferVO;
import org.fiware.contract.repository.OfferRepository;
import org.fiware.contract.repository.OrganizationRepository;
import org.fiware.contract.repository.ServiceRepository;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Controller
public class OfferApiController implements OfferApi {

	private final OfferRepository offerRepository;
	private final OrganizationRepository organizationRepository;
	private final ServiceRepository serviceRepository;

	private final EntityMapper entityMapper;


	@Override
	public HttpResponse<Object> createOffer(@Valid @NotNull OfferVO offerVO) {
		offerVO.setId(UUID.randomUUID().toString());
		Offer offer = entityMapper.offerVoToOffer(
				offerVO,
				organizationRepository,
				serviceRepository);
		return HttpResponse.created(offerRepository.createOffer(offer));
	}

	@Override
	public Optional<OfferVO> getOfferById(String offerId) {
		return offerRepository.getOffer(IdHelper.getUriFromId("offer", offerId)).map(entityMapper::offerToOfferVO);
	}

	@Override
	public List<OfferVO> getOffers() {
		return offerRepository.getOffers().stream().filter(Objects::nonNull).map(entityMapper::offerToOfferVO).collect(Collectors.toList());
	}


}
