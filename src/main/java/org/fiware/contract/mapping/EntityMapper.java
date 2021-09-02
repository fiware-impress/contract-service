package org.fiware.contract.mapping;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.fiware.broker.model.EntityVO;
import org.fiware.broker.model.GeoPropertyVO;
import org.fiware.broker.model.PropertyVO;
import org.fiware.broker.model.RelationshipVO;
import org.fiware.contract.IdHelper;
import org.fiware.contract.model.AddressVO;
import org.fiware.contract.model.ContactPoint;
import org.fiware.contract.model.GeneralThing;
import org.fiware.contract.model.ItemAvailability;
import org.fiware.contract.model.MeasurementPoint;
import org.fiware.contract.model.MeasurementPointVO;
import org.fiware.contract.model.Offer;
import org.fiware.contract.model.OfferVO;
import org.fiware.contract.model.Order;
import org.fiware.contract.model.OrderVO;
import org.fiware.contract.model.Organization;
import org.fiware.contract.model.OrganizationVO;
import org.fiware.contract.model.PostalAddress;
import org.fiware.contract.model.PriceDefinition;
import org.fiware.contract.model.PriceDefinitionVO;
import org.fiware.contract.model.ProviderVO;
import org.fiware.contract.model.SmartService;
import org.fiware.contract.model.SmartServiceVO;
import org.fiware.contract.model.Thing;
import org.fiware.contract.repository.OfferRepository;
import org.fiware.contract.repository.OrderRepository;
import org.fiware.contract.repository.OrganizationRepository;
import org.fiware.contract.repository.ServiceRepository;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.fiware.contract.model.PaymentMethod.BY_INVOICE;

@Mapper(componentModel = "jsr330")
public interface EntityMapper {

	ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	// OFFERS

	default Offer offerVoToOffer(OfferVO offerVO, OrganizationRepository organizationRepository, ServiceRepository serviceRepository) {
		Offer offer = new Offer();
		offer.setIdentifier(IdHelper.getUriFromId("offer", offerVO.getId()));
		offer.setAcceptedPaymentMethod(BY_INVOICE);
		offer.setAreaServed(offerVO.getAreaServed());
		offer.setAvailability(offerVO.getItemAvailable() ? ItemAvailability.IN_STOCK : ItemAvailability.OUT_OF_STOCK);
		offer.setCategory(offerVO.getCategory());
		offer.setItemOffered(serviceRepository.getService(IdHelper.getUriFromId("smart-service", offerVO.serviceId())).orElseThrow(() -> new RuntimeException("No such service exists.")));
		offer.setSeller(organizationRepository.getOrganizationById(IdHelper.getUriFromId("organization", offerVO.sellerId())));
		return offer;
	}

	default OfferVO offerToOfferVO(Offer offer) {
		OfferVO offerVO = new OfferVO();
		offerVO.setId(IdHelper.getIdFromIdentifier(offer.getIdentifier()));
		offerVO.areaServed(offer.getAreaServed());
		offerVO.category(offer.getCategory());
		offerVO.itemAvailable(offer.getAvailability() == ItemAvailability.IN_STOCK ? true : false);
		offerVO.serviceId(IdHelper.getIdFromIdentifier(offer.getItemOffered().getIdentifier()));
		offerVO.sellerId(IdHelper.getIdFromIdentifier(offer.getSeller().getIdentifier()));
		return offerVO;
	}

	default Offer entityVoToOffer(EntityVO entityVO, OrganizationRepository organizationRepository, ServiceRepository serviceRepository) {
		try {

			Offer offer = new Offer();
			offer.setIdentifier(entityVO.getId());

			Map<String, Object> additionalProperties = entityVO.getAdditionalProperties();

			String sellerId = (String) ((Map) additionalProperties.get("seller")).get("object");
			String serviceId = (String) ((Map) additionalProperties.get("itemOffered")).get("object");

			offer.setSeller(organizationRepository.getOrganizationById(URI.create(sellerId)));
			offer.setItemOffered(serviceRepository.getService(URI.create(serviceId)).get());

			offer.setAcceptedPaymentMethod(BY_INVOICE);
			offer.setAvailability(ItemAvailability.valueOf((String) ((Map) additionalProperties.get("availability")).get("value")));
			offer.setAreaServed((String) ((Map) additionalProperties.get("areaServed")).get("value"));
			offer.setCategory((String) ((Map) additionalProperties.get("category")).get("value"));


			return offer;
		} catch (RuntimeException e) {
			return null;
		}
	}


	default EntityVO offerToEntityVO(URL context, Offer offer) {
		EntityVO entityVO = getEntityVO("Offer", context, offer.getIdentifier());
		entityVO.setAdditionalProperties(offerToProperties(offer));
		return entityVO;
	}

	// ORGANIZATIONS

	default EntityVO organizationToEntityVO(URL context, Organization organization) {
		EntityVO entityVO = getEntityVO("Organization", context, organization.getIdentifier());
		entityVO.setAdditionalProperties(organizationToProperties(organization));
		return entityVO;
	}

	default Organization entityVoToOrganization(EntityVO entityVO) {
		Map<String, Object> additionalProperties = entityVO.getAdditionalProperties();

		Organization organization = new Organization();
		organization.setIdentifier(entityVO.getId());
		organization.setAddress(OBJECT_MAPPER.convertValue(((Map) additionalProperties.get("address")).get("value"), PostalAddress.class));
		organization.setContactPoint(OBJECT_MAPPER.convertValue(((Map) additionalProperties.get("contactPoint")).get("value"), ContactPoint.class));
		organization.setLegalName((String) ((Map) additionalProperties.get("legalName")).get("value"));

		return organization;
	}

	default Organization organizationVoToOrganization(OrganizationVO organizationVO) {
		Organization organization = new Organization();
		organization.setIdentifier(IdHelper.getUriFromId("organization", organizationVO.getId()));
		organization.setLegalName(organizationVO.getLegalName());

		ContactPoint contactPoint = new ContactPoint();
		contactPoint.setContactType(organizationVO.getContactType());
		contactPoint.setEmail(organizationVO.getEmail());
		contactPoint.setTelephone(organizationVO.getTelephone());
		organization.setContactPoint(contactPoint);

		PostalAddress postalAddress = new PostalAddress();
		postalAddress.setAddressCountry(organizationVO.getAddressCountry());
		postalAddress.setAddressLocality(organizationVO.getAddressLocality());
		postalAddress.setAddressRegion(organizationVO.getAddressRegion());
		postalAddress.setPostalCode(organizationVO.getPostalCode());
		postalAddress.setPostOfficeBoxNumber(organizationVO.getPostOfficeBoxNumber());
		postalAddress.setStreetAddress(organizationVO.getStreetAddress());
		organization.setAddress(postalAddress);

		return organization;
	}

	default OrganizationVO organizationToOrganizationVo(Organization organization) {
		OrganizationVO organizationVO = new OrganizationVO();
		organizationVO.setId(IdHelper.getIdFromIdentifier(organization.getIdentifier()));
		organizationVO.setLegalName(organization.getLegalName());

		ContactPoint contactPoint = organization.getContactPoint();
		organizationVO.setContactType(contactPoint.getContactType());
		organizationVO.setEmail(contactPoint.getEmail());
		organizationVO.setTelephone(contactPoint.getTelephone());

		PostalAddress postalAddress = organization.getAddress();
		organizationVO.setAddressCountry(postalAddress.getAddressCountry());
		organizationVO.setAddressLocality(postalAddress.getAddressLocality());
		organizationVO.setAddressRegion(postalAddress.getAddressRegion());
		organizationVO.setPostalCode(postalAddress.getPostalCode());
		organizationVO.setPostOfficeBoxNumber(postalAddress.getPostOfficeBoxNumber());
		organizationVO.setStreetAddress(postalAddress.getStreetAddress());

		return organizationVO;
	}

	// SMART SERVICES

	default SmartService smartServiceVoToSmartService(SmartServiceVO smartServiceVO) {
		SmartService service = new SmartService();
		service.setIdentifier(IdHelper.getUriFromId("smart-service", smartServiceVO.getId()));

		service.setCategory(smartServiceVO.getCategory());
		service.setServiceType(smartServiceVO.getServiceType());

		service.setPriceDefinitions(smartServiceVO
				.getPriceDefinitions()
				.stream()
				.map(this::priceDefinitionVoToPriceDefinition)
				.collect(Collectors.toList()));
		return service;
	}

	default SmartServiceVO smartServiceToSmartServiceVo(SmartService smartService) {
		SmartServiceVO smartServiceVO = new SmartServiceVO();
		smartServiceVO.setId(IdHelper.getIdFromIdentifier(smartService.getIdentifier()));
		smartServiceVO.setCategory(smartService.getCategory());
		smartServiceVO.setServiceType(smartService.getServiceType());

		smartServiceVO.setPriceDefinitions(
				smartService
						.getPriceDefinitions()
						.stream()
						.map(this::priceDefinitionToPriceDefinitionVO)
						.collect(Collectors.toList()));
		return smartServiceVO;
	}

	default EntityVO smartServiceToEntityVO(SmartService smartService, URL context) {
		EntityVO entityVO = getEntityVO("SmartService", context, smartService.getIdentifier());
		entityVO.setAdditionalProperties(smartServiceToProperties(smartService));
		return entityVO;
	}

	default SmartService entityVoToSmartService(EntityVO entityVO) {
		Map<String, Object> additionalProperties = entityVO.getAdditionalProperties();

		SmartService smartService = new SmartService();
		smartService.setIdentifier(entityVO.getId());

		smartService.setServiceType((String) ((Map) additionalProperties.get("serviceType")).get("value"));
		smartService.setCategory((String) ((Map) additionalProperties.get("category")).get("value"));

		Object priceDefinitionsValue = ((Map) additionalProperties.get("priceDefinitions")).get("value");
		if (priceDefinitionsValue instanceof Map) {
			PriceDefinition priceDefinition = OBJECT_MAPPER.convertValue((Map) priceDefinitionsValue, PriceDefinition.class);
			smartService.setPriceDefinitions(List.of(priceDefinition));
		} else {
			List<Map> priceDefinitionList = ((List) priceDefinitionsValue);
			List<PriceDefinition> priceDefinitions = priceDefinitionList.stream()
					.map(definitonMap -> OBJECT_MAPPER.convertValue(definitonMap, PriceDefinition.class))
					.collect(Collectors.toList());
			smartService.setPriceDefinitions(priceDefinitions);
		}

		return smartService;
	}

	PriceDefinition priceDefinitionVoToPriceDefinition(PriceDefinitionVO priceDefinitionVO);

	PriceDefinitionVO priceDefinitionToPriceDefinitionVO(PriceDefinition priceDefinition);

	MeasurementPoint measurementPointVoToMeasurementPoint(MeasurementPointVO measurementPointVO);

	MeasurementPointVO measurementPointToMeasurementPointVo(MeasurementPoint measurementPoint);

	default ProviderVO generalThingToProviderVo(GeneralThing generalThing) {
		return new ProviderVO()
				.id(IdHelper.getIdFromIdentifier(generalThing.getIdentifier()))
				.type(generalThing.getThingType());
	}

	default GeneralThing providerVoToGeneralThing(ProviderVO providerVO) {
		GeneralThing generalThing = new GeneralThing();
		generalThing.setThingType(providerVO.type());
		generalThing.setIdentifier(IdHelper.getUriFromId(providerVO.type(), providerVO.id()));
		return generalThing;
	}

	default Number doubleToNumber(Double doubleValue) {
		return doubleValue;
	}

	default Double numberToDouble(Number number) {
		return number.doubleValue();
	}

	// THING

	default GeneralThing entityVoToGeneralThing(EntityVO entityVO) {
		GeneralThing generalThing = new GeneralThing();
		generalThing.setIdentifier(entityVO.getId());
		generalThing.setThingType(entityVO.getType());
		return generalThing;
	}

	// ORDER

	default Order orderVoToOrder(OrderVO orderVO, OrganizationRepository organizationRepository, OfferRepository offerRepository) {
		Order order = new Order();
		order.setIdentifier(IdHelper.getUriFromId("order", orderVO.id()));
		order.setAcceptedOffer(offerRepository.getOffer(IdHelper.getUriFromId("offer", orderVO.getAcceptedOfferId())).orElseThrow(() -> new RuntimeException("No such offer exists.")));
		order.setConfirmationNumber(orderVO.getConfirmationNumber());
		order.setCustomer(organizationRepository.getOrganizationById(IdHelper.getUriFromId("organization", orderVO.getCustomerId())));
		order.setSeller(organizationRepository.getOrganizationById(IdHelper.getUriFromId("organization", orderVO.getSellerId())));
		order.setDiscount(orderVO.getDiscount());
		order.setDiscountCurrency(orderVO.getDiscountCurrency());
		order.setOrderNumber(orderVO.getOrderNumber());
		// still to do
		order.setPartOfInvoice(null);
		order.setPaymentMethod(BY_INVOICE);
		order.setBillingAddress(addressVoToPostalAddress(orderVO.getBillingAddress()));
		return order;
	}

	PostalAddress addressVoToPostalAddress(AddressVO addressVO);

	AddressVO postalAddressToAddressVo(PostalAddress postalAddress);

	default OrderVO orderToOrderVo(Order order) {
		OrderVO orderVO = new OrderVO();
		orderVO.setId(IdHelper.getIdFromIdentifier(order.getIdentifier()));
		orderVO.setConfirmationNumber(order.getConfirmationNumber());
		orderVO.setOrderNumber(order.getOrderNumber());
		orderVO.setCustomerId(IdHelper.getIdFromIdentifier(order.getCustomer().getIdentifier()));
		orderVO.setSellerId(IdHelper.getIdFromIdentifier(order.getSeller().getIdentifier()));
		orderVO.setDiscount(order.getDiscount().doubleValue());
		orderVO.setDiscountCurrency(order.getDiscountCurrency());
		orderVO.setInvoiceId("temp");
		orderVO.setPaymentMethod(BY_INVOICE.value());
		orderVO.setBillingAddress(postalAddressToAddressVo(order.getBillingAddress()));
		orderVO.setAcceptedOfferId(IdHelper.getIdFromIdentifier(order.getAcceptedOffer().getIdentifier()));
		return orderVO;
	}

	default EntityVO orderToEntityVo(Order order, URL context) {
		EntityVO entityVO = getEntityVO("Order", context, order.getIdentifier());
		entityVO.setAdditionalProperties(orderToProperties(order));
		return entityVO;
	}

	default Map<String, Object> orderToProperties(Order order) {
		Map<String, Object> objectMap = new HashMap<>();
		objectMap.put("billingAddress", asProperty(order.getBillingAddress()));
		objectMap.put("confirmationNumber", asProperty(order.getConfirmationNumber()));
		objectMap.put("discount", asProperty(order.getDiscount()));
		objectMap.put("discountCurrency", asProperty(order.getDiscountCurrency()));
		objectMap.put("paymentMethod", asProperty(BY_INVOICE));
		objectMap.put("seller", asRelationShip(order.getSeller().getIdentifier()));
		objectMap.put("customer", asRelationShip(order.getCustomer().getIdentifier()));
		objectMap.put("acceptedOffer", asRelationShip(order.getAcceptedOffer().getIdentifier()));
		objectMap.put("orderNumber", asProperty(order.getOrderNumber()));
		return objectMap;
	}

	default Order entityVoToOrder(EntityVO entityVO, OrganizationRepository organizationRepository, OfferRepository offerRepository) {
		Map<String, Object> additionalProperties = entityVO.getAdditionalProperties();

		Order order = new Order();
		order.setIdentifier(entityVO.getId());
		order.setBillingAddress(OBJECT_MAPPER.convertValue(((Map) additionalProperties.get("billingAddress")).get("value"), PostalAddress.class));
		order.setConfirmationNumber((String) ((Map) additionalProperties.get("confirmationNumber")).get("value"));
		order.setDiscount((Number) ((Map) additionalProperties.get("discount")).get("value"));
		order.setDiscountCurrency((String) ((Map) additionalProperties.get("discountCurrency")).get("value"));
		order.setOrderNumber((String) ((Map) additionalProperties.get("orderNumber")).get("value"));
		order.setPaymentMethod(BY_INVOICE);

		String offerId = (String) ((Map) additionalProperties.get("acceptedOffer")).get("object");
		String sellerId = (String) ((Map) additionalProperties.get("seller")).get("object");
		String customerId = (String) ((Map) additionalProperties.get("customer")).get("object");

		order.setAcceptedOffer(offerRepository.getOffer(URI.create(offerId)).orElseThrow(() -> new RuntimeException("No such offer exists.")));
		order.setSeller(organizationRepository.getOrganizationById(URI.create(sellerId)));
		order.setCustomer(organizationRepository.getOrganizationById(URI.create(customerId)));

		return order;
	}

	// HELPER

	private EntityVO getEntityVO(String type, URL context, URI id) {
		EntityVO entityVO = new EntityVO();
		entityVO.atContext(context);
		entityVO.type(type);
		entityVO.id(id);
		entityVO.location(null);
		entityVO.observationSpace(null);
		entityVO.operationSpace(null);
		return entityVO;
	}

	private Map<String, Object> organizationToProperties(Organization organization) {
		Map<String, Object> objectMap = new HashMap<>();
		objectMap.put("address", asProperty(organization.getAddress()));
		objectMap.put("contactPoint", asProperty(organization.getContactPoint()));
		objectMap.put("legalName", asProperty(organization.getLegalName()));
		return objectMap;
	}

	private Map<String, Object> offerToProperties(Offer offer) {
		Map<String, Object> objectMap = new HashMap<>();
		objectMap.put("acceptedPaymentMethod", asProperty(BY_INVOICE));
		// TODO: support geo json
		objectMap.put("areaServed", asProperty(offer.getAreaServed()));
		objectMap.put("availability", asProperty(offer.getAvailability()));
		objectMap.put("category", asProperty(offer.getCategory()));
		objectMap.put("seller", asRelationShip(offer.getSeller().getIdentifier()));
		objectMap.put("itemOffered", asRelationShip(offer.getItemOffered().getIdentifier()));
		return objectMap;
	}

	private Map<String, Object> smartServiceToProperties(SmartService smartService) {
		Map<String, Object> objectMap = new HashMap<>();
		objectMap.put("category", asProperty(smartService.getCategory()));
		objectMap.put("serviceType", asProperty(smartService.getServiceType()));

		objectMap.put("priceDefinitions", asProperty(smartService.getPriceDefinitions()));
		return objectMap;
	}

	private PropertyVO asProperty(Object value) {
		PropertyVO propertyVO = new PropertyVO();
		propertyVO.setType(PropertyVO.Type.PROPERTY);
		propertyVO.setValue(value);
		return propertyVO;
	}

	private GeoPropertyVO asGeoProperty(Object value) {
		GeoPropertyVO propertyVO = new GeoPropertyVO();
		propertyVO.setValue(value);
		propertyVO.setType(GeoPropertyVO.Type.GEOPROPERTY);
		return propertyVO;
	}

	private RelationshipVO asRelationShip(URI objectURI) {
		RelationshipVO relationshipVO = new RelationshipVO();
		relationshipVO.setObject(objectURI);
		relationshipVO.setType(RelationshipVO.Type.RELATIONSHIP);
		return relationshipVO;
	}
}
