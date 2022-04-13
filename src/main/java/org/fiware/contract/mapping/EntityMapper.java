package org.fiware.contract.mapping;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.fiware.broker.model.EntityFragmentVO;
import org.fiware.broker.model.EntityVO;
import org.fiware.broker.model.GeoPropertyVO;
import org.fiware.broker.model.PropertyVO;
import org.fiware.broker.model.RelationshipVO;
import org.fiware.contract.IdHelper;
import org.fiware.contract.model.AddressVO;
import org.fiware.contract.model.BankAccount;
import org.fiware.contract.model.BankAccountVO;
import org.fiware.contract.model.ContactPoint;
import org.fiware.contract.model.Invoice;
import org.fiware.contract.model.InvoiceVO;
import org.fiware.contract.model.ItemAvailability;
import org.fiware.contract.model.MeasurementPoint;
import org.fiware.contract.model.MeasurementPointVO;
import org.fiware.contract.model.MonetaryAmount;
import org.fiware.contract.model.Offer;
import org.fiware.contract.model.OfferVO;
import org.fiware.contract.model.Order;
import org.fiware.contract.model.OrderVO;
import org.fiware.contract.model.Organization;
import org.fiware.contract.model.OrganizationVO;
import org.fiware.contract.model.PaymentStatus;
import org.fiware.contract.model.PostalAddress;
import org.fiware.contract.model.PriceDefinition;
import org.fiware.contract.model.PriceDefinitionVO;
import org.fiware.contract.model.ProviderVO;
import org.fiware.contract.model.SmartService;
import org.fiware.contract.model.SmartServiceVO;
import org.fiware.contract.model.Thing;
import org.fiware.contract.repository.MeasurementPointRepository;
import org.fiware.contract.repository.OfferRepository;
import org.fiware.contract.repository.OrderRepository;
import org.fiware.contract.repository.OrganizationRepository;
import org.fiware.contract.repository.PriceDefinitionRepository;
import org.fiware.contract.repository.ServiceRepository;
import org.fiware.contract.repository.ThingRepository;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import java.net.URI;
import java.net.URL;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.fiware.contract.model.PaymentMethod.BY_INVOICE;

@Mapper(componentModel = "jsr330")
public interface EntityMapper {

	ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	// Thing

	default Thing entityVoToThing(EntityVO entityVO) {
		Thing thing = new Thing();
		thing.setIdentifier(entityVO.getId());
		return thing;
	}

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
		organization.setBankAccount(OBJECT_MAPPER.convertValue(((Map) additionalProperties.get("bankAccount")).get("value"), BankAccount.class));
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

		BankAccount bankAccount = map(organizationVO.getBankAccount());

		organization.setBankAccount(bankAccount);
		organization.setAddress(postalAddress);

		return organization;
	}

	BankAccount map(BankAccountVO bankAccountVO);

	BankAccountVO map(BankAccount bankAccount);

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

		BankAccountVO bankAccountVO = map(organization.getBankAccount());
		organizationVO.setBankAccount(bankAccountVO);

		return organizationVO;
	}

	// SMART SERVICES

	default SmartService smartServiceVoToSmartService(SmartServiceVO smartServiceVO, ThingRepository thingRepository) {
		SmartService service = new SmartService();
		service.setIdentifier(IdHelper.getUriFromId("smart-service", smartServiceVO.getId()));

		service.setCategory(smartServiceVO.getCategory());
		service.setServiceType(smartServiceVO.getServiceType());

		service.setPriceDefinitions(smartServiceVO
				.getPriceDefinitions()
				.stream()
				.map(pd -> priceDefinitionVoToPriceDefinition(pd, thingRepository))
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

	default SmartService entityVoToSmartService(EntityVO entityVO, PriceDefinitionRepository priceDefinitionRepository) {
		Map<String, Object> additionalProperties = entityVO.getAdditionalProperties();

		SmartService smartService = new SmartService();
		smartService.setIdentifier(entityVO.getId());

		smartService.setServiceType((String) ((Map) additionalProperties.get("serviceType")).get("value"));
		smartService.setCategory((String) ((Map) additionalProperties.get("category")).get("value"));

		if (additionalProperties.get("priceDefinitions") instanceof List) {
			List<PriceDefinition> priceDefinitions = (List<PriceDefinition>) ((List) additionalProperties.get("priceDefinitions"))
					.stream()
					.map(property -> ((Map) property).get("object"))
					.map(stringId -> URI.create((String) stringId))
					.map(uriId -> priceDefinitionRepository.getPriceDefinitionById((URI) uriId))
					.collect(Collectors.toList());
			smartService.setPriceDefinitions(priceDefinitions);
		} else {
			String priceDefinitionsId = (String) ((Map) additionalProperties.get("priceDefinitions")).get("object");
			smartService.setPriceDefinitions(List.of(priceDefinitionRepository.getPriceDefinitionById(URI.create(priceDefinitionsId))));
		}

		return smartService;
	}

	default PriceDefinition priceDefinitionVoToPriceDefinition(PriceDefinitionVO priceDefinitionVO, ThingRepository thingRepository) {
		PriceDefinition priceDefinition = new PriceDefinition();
		priceDefinition.setUnitCode(priceDefinitionVO.getUnitCode());
		priceDefinition.setPriceCurrency(priceDefinitionVO.getPriceCurrency());
		priceDefinition.setQuantity(priceDefinitionVO.getQuantity());
		priceDefinition.setPrice(priceDefinitionVO.getPrice());
		priceDefinition.setMeasurementPoint(measurementPointVoToMeasurementPoint(priceDefinitionVO.getMeasurementPoint(), thingRepository));
		return priceDefinition;
	}

	PriceDefinitionVO priceDefinitionToPriceDefinitionVO(PriceDefinition priceDefinition);

	default MeasurementPoint measurementPointVoToMeasurementPoint(MeasurementPointVO measurementPointVO, ThingRepository thingRepository) {
		MeasurementPoint measurementPoint = new MeasurementPoint();
		measurementPoint.setMeasurementQuery(measurementPointVO.getMeasurementQuery());
		measurementPoint.setUnitCode(measurementPointVO.getUnitCode());
		measurementPoint.setProvider(thingRepository
				.getThingById(URI.create(measurementPointVO.getProvider().getId())).orElseThrow(() -> new RuntimeException(String.format("Was not able to get thing."))));
		return measurementPoint;
	}

	default ProviderVO map(Thing thing) {
		return new ProviderVO().type(thing.getIdentifier().toString().split(":")[2]).id(thing.getIdentifier().toString());
	}

	MeasurementPointVO measurementPointToMeasurementPointVo(MeasurementPoint measurementPoint);


	default Number doubleToNumber(Double doubleValue) {
		return doubleValue;
	}

	default Double numberToDouble(Number number) {
		return number.doubleValue();
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

	// Invoices

	default EntityVO invoiceToEntityVO(URL context, Invoice invoice) {
		EntityVO entityVO = getEntityVO("Invoice", context, invoice.getIdentifier());
		entityVO.setAdditionalProperties(invoiceToProperties(invoice));
		return entityVO;
	}

	private Map<String, Object> invoiceToProperties(Invoice invoice) {
		Map<String, Object> objectMap = new HashMap<>();
		objectMap.put("accountId", asProperty(invoice.getAccountId()));
		objectMap.put("confirmationNumber", asProperty(invoice.getConfirmationNumber()));
		objectMap.put("paymentDueDate", asProperty(invoice.getPaymentDueDate()));
		objectMap.put("paymentMethod", asProperty(invoice.getPaymentMethod()));
		objectMap.put("paymentStatus", asProperty(invoice.getPaymentStatus().value()));
		objectMap.put("customer", asRelationShip(invoice.getCustomer().getIdentifier()));
		objectMap.put("producer", asRelationShip(invoice.getProducer().getIdentifier()));
		List<RelationshipVO> referencesOrderList = invoice.getReferencesOrder()
				.stream()
				.map(refOrder -> asRelationShip(refOrder.getIdentifier()))
				.collect(Collectors.toList());
		objectMap.put("referencesOrder", referencesOrderList);
		objectMap.put("totalPaymentDue", asProperty(invoice.getTotalPaymentDue()));
		return objectMap;
	}

	default Invoice entityVoToInvoice(EntityVO entityVO, OrganizationRepository organizationRepository, OrderRepository orderRepository) {
		Map<String, Object> additionalProperties = entityVO.getAdditionalProperties();

		Invoice invoice = new Invoice();
		invoice.setIdentifier(entityVO.getId());
		invoice.setAccountId((String) ((Map) additionalProperties.get("accountId")).get("value"));
		invoice.setConfirmationNumber((String) ((Map) additionalProperties.get("confirmationNumber")).get("value"));
		invoice.setPaymentDueDate(Instant.ofEpochSecond(((Number) ((Map) additionalProperties.get("paymentDueDate")).get("value")).longValue()));
		invoice.setPaymentMethod(BY_INVOICE);
		invoice.setCreationDate(entityVO.createdAt());
		invoice.setPaymentStatus(PaymentStatus.getByValue(((String) ((Map) additionalProperties.get("paymentStatus")).get("value"))));
		String producerId = (String) ((Map) additionalProperties.get("producer")).get("object");
		String customerId = (String) ((Map) additionalProperties.get("customer")).get("object");
		invoice.setProducer(organizationRepository.getOrganizationById(URI.create(producerId)));
		invoice.setCustomer(organizationRepository.getOrganizationById(URI.create(customerId)));

		if (entityVO.getAdditionalProperties().get("referencesOrder") instanceof List) {
			List<Order> orders = (List<Order>) ((List) additionalProperties.get("referencesOrder"))
					.stream()
					.map(property -> ((Map) property).get("object"))
					.map(stringId -> URI.create((String) stringId))
					.map(uriId -> orderRepository.getOrderById((URI) uriId))
					.filter(optionalOrder -> ((Optional<Order>) optionalOrder).isPresent())
					.map(presentOrder -> ((Optional<Order>) presentOrder).get())
					.collect(Collectors.toList());
			invoice.setReferencesOrder(orders);
		} else {
			String orderId = (String) ((Map) additionalProperties.get("referencesOrder")).get("object");
			invoice.setReferencesOrder(orderRepository.getOrderById(URI.create(orderId)).map(o -> List.of(o)).orElse(List.of()));
		}

		invoice.setTotalPaymentDue(OBJECT_MAPPER.convertValue(((Map) additionalProperties.get("totalPaymentDue")).get("value"), MonetaryAmount.class));
		return invoice;
	}

	@Mappings({
			@Mapping(source = "identifier", target = "id"),
			@Mapping(source = "referencesOrder", target = "referencesOrders"),
			@Mapping(source = "totalPaymentDue.value", target = "amount"),
			@Mapping(source = "creationDate", target = "creationDate")
	})
	InvoiceVO invoiceToInvoiceVO(Invoice invoice);

	default String uriToString(URI uri) {
		return uri.toString();
	}

	// MEASUREMENT POINT

	default EntityVO measurementPointToEntityVO(MeasurementPoint measurementPoint, URL contextURL) {
		EntityVO entityVO = getEntityVO("MeasurementPoint", contextURL, measurementPoint.getIdentifier());
		entityVO.setAdditionalProperties(Map.of(
				"unitCode", asProperty(measurementPoint.getUnitCode()),
				"measurementQuery", asProperty(measurementPoint.getMeasurementQuery()),
				"provider", asRelationShip(measurementPoint.getProvider().getIdentifier())
		));
		return entityVO;
	}

	default MeasurementPoint entityVoToMeasurementPoint(EntityVO entityVO, ThingRepository thingRepository) {
		Map<String, Object> additionalProperties = entityVO.getAdditionalProperties();

		MeasurementPoint measurementPoint = new MeasurementPoint();

		measurementPoint.setIdentifier(entityVO.getId());
		measurementPoint.setUnitCode((String) ((Map) additionalProperties.get("unitCode")).get("value"));
		measurementPoint.setMeasurementQuery((String) ((Map) additionalProperties.get("measurementQuery")).get("value"));

		String providerId = (String) ((Map) additionalProperties.get("provider")).get("object");
		measurementPoint.setProvider(thingRepository.getThingById(URI.create(providerId)).orElseThrow(() -> new RuntimeException(String.format("Provider %s not found.", providerId))));

		return measurementPoint;
	}

	// Price Definition

	default EntityVO priceDefinitionToEntityVo(PriceDefinition priceDefinition, URL contextURL) {
		EntityVO entityVO = getEntityVO("PriceDefinition", contextURL, priceDefinition.getIdentifier());
		entityVO.setAdditionalProperties(Map.of(
				"unitCode", asProperty(priceDefinition.getUnitCode()),
				"quantity", asProperty(priceDefinition.getQuantity()),
				"price", asProperty(priceDefinition.getPrice()),
				"priceCurrency", asProperty(priceDefinition.getPriceCurrency()),
				"measurementPoint", asRelationShip(priceDefinition.getMeasurementPoint().getIdentifier())
		));
		return entityVO;
	}

	default PriceDefinition entityVoToPriceDefinition(EntityVO entityVO, MeasurementPointRepository measurementPointRepository) {
		Map<String, Object> additionalProperties = entityVO.getAdditionalProperties();

		PriceDefinition priceDefinition = new PriceDefinition();
		priceDefinition.setIdentifier(entityVO.getId());
		priceDefinition.setPrice((Number) ((Map) additionalProperties.get("price")).get("value"));
		priceDefinition.setPriceCurrency((String) ((Map) additionalProperties.get("priceCurrency")).get("value"));
		priceDefinition.setQuantity((Number) ((Map) additionalProperties.get("quantity")).get("value"));
		priceDefinition.setUnitCode((String) ((Map) additionalProperties.get("unitCode")).get("value"));

		String measurementPointId = (String) ((Map) additionalProperties.get("measurementPoint")).get("object");
		priceDefinition.setMeasurementPoint(measurementPointRepository.getMeasurementPointById(URI.create(measurementPointId)));

		return priceDefinition;
	}

	default EntityFragmentVO entityToEntityFragmentVO(EntityVO entityVO) {
		EntityFragmentVO entityFragmentVO = new EntityFragmentVO();
		entityFragmentVO.setAdditionalProperties(entityVO.getAdditionalProperties());
		entityFragmentVO.setAtContext(entityVO.getAtContext());
		entityFragmentVO.setCreatedAt(entityVO.getCreatedAt());
		entityFragmentVO.setModifiedAt(entityVO.getModifiedAt());
		entityFragmentVO.setLocation(entityVO.getLocation());
		entityFragmentVO.setObservationSpace(entityVO.getObservationSpace());
		entityFragmentVO.setOperationSpace(entityVO.getOperationSpace());
		return entityFragmentVO;
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
		objectMap.put("bankAccount", asProperty(organization.getBankAccount()));
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

		objectMap.put("priceDefinitions", smartService
				.getPriceDefinitions()
				.stream()
				.map(priceDefinition -> asRelationShip(priceDefinition.getIdentifier())).collect(Collectors.toList()));
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
