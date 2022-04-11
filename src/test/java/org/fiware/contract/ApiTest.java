package org.fiware.contract;


import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import lombok.RequiredArgsConstructor;
import org.fiware.broker.api.SubscriptionsApiClient;
import org.fiware.broker.model.EndpointReceiverInfoVO;
import org.fiware.broker.model.EndpointVO;
import org.fiware.broker.model.EntityFragmentVO;
import org.fiware.broker.model.EntityInfoVO;
import org.fiware.broker.model.EntityVO;
import org.fiware.broker.model.GeoPropertyVO;
import org.fiware.broker.model.NotificationParamsVO;
import org.fiware.broker.model.PointVO;
import org.fiware.broker.model.PropertyVO;
import org.fiware.broker.model.SubscriptionVO;
import org.fiware.contract.api.OfferApiTestClient;
import org.fiware.contract.api.OrderApiTestClient;
import org.fiware.contract.api.OrganizationApiTestClient;
import org.fiware.contract.api.SmartServiceApiTestClient;
import org.fiware.contract.configuration.GeneralProperties;
import org.fiware.contract.model.AddressVO;
import org.fiware.contract.model.BankAccountVO;
import org.fiware.contract.model.MeasurementPointVO;
import org.fiware.contract.model.OfferVO;
import org.fiware.contract.model.OrderVO;
import org.fiware.contract.model.OrganizationVO;
import org.fiware.contract.model.PaymentMethod;
import org.fiware.contract.model.PriceDefinitionVO;
import org.fiware.contract.model.ProviderVO;
import org.fiware.contract.model.SmartServiceVO;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@RequiredArgsConstructor
public abstract class ApiTest {

	private static final URI CORE_CONTEXT = URI.create("https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld");


	private final TestThingRepository testThingRepository;
	private final SubscriptionsApiClient subscriptionsApiClient;
	private final GeneralProperties generalProperties;

	private final OfferApiTestClient offerApiTestClient;
	private final OrganizationApiTestClient organizationApiTestClient;
	private final SmartServiceApiTestClient serviceApiTestClient;
	private final OrderApiTestClient orderApiTestClient;

	@Test
	public void test() {

		PropertyVO usageProperty = new PropertyVO()
				.type(PropertyVO.Type.PROPERTY)
				.value(10);

		PointVO pointVO = new PointVO();
		pointVO.type(PointVO.Type.POINT);
		pointVO.coordinates().add(51.24752);
		pointVO.coordinates().add(13.87789);

		GeoPropertyVO locationProperty = new GeoPropertyVO()
				.type(GeoPropertyVO.Type.GEOPROPERTY)
				.value(pointVO);
		PropertyVO modelPropertyVO = new PropertyVO()
				.type(PropertyVO.Type.PROPERTY)
				.value("Euro SSG 130");
		PropertyVO inUseProperty = new PropertyVO()
				.type(PropertyVO.Type.PROPERTY)
				.value(true);

		EntityVO testEntity = new EntityVO()
				.atContext(CORE_CONTEXT)
				.id(URI.create("urn:ngsi-ld:crane:my-test-entity"))
				.type("crane")
				.location(locationProperty)
				.observationSpace(null)
				.operationSpace(null)
				.setAdditionalProperties(Map.of("currentUsage", usageProperty, "model", modelPropertyVO, "inUse", inUseProperty));

		testThingRepository.createThing(testEntity);

		HttpResponse<?> organizationCreationResponse = organizationApiTestClient.createOrganization(
				new OrganizationVO()
						.addressCountry("Germany")
						.addressRegion("Saxony")
						.addressLocality("Dresden")
						.legalName("MyTestCompany GmbH")
						.email("test@company.org")
						.telephone("0351/1234567")
						.contactType("Support")
						.postalCode("01159")
						.streetAddress("Bergstraße 12")
						.bankAccount(new BankAccountVO().id("My-Bank-Account").brand("Sparkasse").routingNumber("123456789")));

		assertEquals(HttpStatus.CREATED, organizationCreationResponse.getStatus(), "The organization should have been created.");
		String locationHeaderOrg = organizationCreationResponse.getHeaders().get("Location");
		assertNotNull(locationHeaderOrg, "A location header should have been returned.");
		assertFalse(locationHeaderOrg.isEmpty(), "The location header should contain an id.");

		HttpResponse<?> customerCreationResponse = organizationApiTestClient.createOrganization(
				new OrganizationVO()
						.addressCountry("Germany")
						.addressRegion("Saxony")
						.addressLocality("Dresden")
						.legalName("Customer Company")
						.email("customer@company.org")
						.telephone("0351/7654321")
						.contactType("Sales")
						.postalCode("01159")
						.streetAddress("Klingenbergerstr. 24")
						.bankAccount(new BankAccountVO().id("My-Bank-Account2").brand("Sparkasse").routingNumber("987654321")));

		assertEquals(HttpStatus.CREATED, customerCreationResponse.getStatus(), "The organization should have been created.");
		String locationHeaderCustomer = customerCreationResponse.getHeaders().get("Location");
		assertNotNull(locationHeaderCustomer, "A location header should have been returned.");
		assertFalse(locationHeaderCustomer.isEmpty(), "The location header should contain an id.");


		HttpResponse<?> serviceCreationResponse = serviceApiTestClient.createService(new SmartServiceVO()
				.serviceType("CraneUsage")
				.category("ConstructionArea")
				.priceDefinitions(List.of(
						new PriceDefinitionVO()
								.price(1.0)
								.unitCode("kg/m")
								.priceCurrency("Euro")
								.quantity(1000.0)
								.measurementPoint(new MeasurementPointVO()
										.measurementQuery("select ev.currentUsage? as CurrentValue from pattern [every ev=iotEvent(cast(cast(currentUsage?, int)%10,int)=0 and type=\"crane\")]")
										.unitCode("kg/m")
										.provider(new ProviderVO()
												.id("urn:ngsi-ld:crane:my-test-entity")
												.type("crane")))))
		);

		assertEquals(HttpStatus.CREATED, serviceCreationResponse.getStatus(), "The service should have been created.");
		String locationHeaderService = serviceCreationResponse.getHeaders().get("Location");
		assertNotNull(locationHeaderService, "A location header should have been returned.");
		assertFalse(locationHeaderService.isEmpty(), "The location header should contain an id.");

		HttpResponse<?> offerCreationResponse = offerApiTestClient.createOffer(
				new OfferVO()
						.areaServed("Dresden")
						.category("ConstructionArea")
						.itemAvailable(true)
						.sellerId(locationHeaderOrg)
						.serviceId(locationHeaderService)
		);

		assertEquals(HttpStatus.CREATED, offerCreationResponse.getStatus(), "The offer should have been created.");
		String locationHeaderOffer = offerCreationResponse.getHeaders().get("Location");
		assertNotNull(locationHeaderOffer, "A location header should have been returned.");
		assertFalse(locationHeaderOffer.isEmpty(), "The location header should contain an id.");

		HttpResponse<?> orderCreationResponse = orderApiTestClient.createOrder(new OrderVO()
				.orderNumber("order-12345")
				.acceptedOfferId(locationHeaderOffer)
				.billingAddress(new AddressVO().streetAddress("Klingenbergerstr. 24")
						.addressCountry("Germany")
						.addressLocality("Dresden")
						.addressRegion("Saxony")
						.postalCode("01159"))
				.confirmationNumber("my-confirmation-nr")
				.customerId(locationHeaderCustomer)
				.discount(0.0)
				.discountCurrency("Euro")
				.sellerId(locationHeaderOrg)
				.paymentMethod(PaymentMethod.BY_INVOICE.value())
		);

		assertEquals(HttpStatus.CREATED, orderCreationResponse.getStatus(), "The order should have been created.");
		String locationHeaderOrder = orderCreationResponse.getHeaders().get("Location");
		assertNotNull(locationHeaderOrder, "A location header should have been returned.");
		assertFalse(locationHeaderOrder.isEmpty(), "The location header should contain an id.");

		HttpResponse<Object> subscriptionCreationResponse = subscriptionsApiClient.createSubscription(new SubscriptionVO()
				.atContext(CORE_CONTEXT)
				.type(SubscriptionVO.Type.SUBSCRIPTION)
				.entities(List.of(new EntityInfoVO().type("crane")))
				.geoQ(null)
				.notification(new NotificationParamsVO()
						.endpoint(new EndpointVO()
								.uri(URI.create("http://perseo-fe:9090/notices"))
								.addReceiverInfoItem(new EndpointReceiverInfoVO().key("Fiware-Service").value("impress"))
						)
				), generalProperties.getTenant()
		);
		assertEquals(HttpStatus.CREATED, subscriptionCreationResponse.getStatus(), "The subscription should have been created.");

		updatePropertyForTestThing(20);
		updatePropertyForTestThing(15);
		updatePropertyForTestThing(30);
		updatePropertyForTestThing(10);
	}

	private void updatePropertyForTestThing(int value) {
		PropertyVO updateUsageProperty = new PropertyVO()
				.type(PropertyVO.Type.PROPERTY)
				.value(value);

		EntityFragmentVO fragmentVO = new EntityFragmentVO()
				.atContext(CORE_CONTEXT)
				.location(null)
				.observationSpace(null)
				.operationSpace(null)
				.setAdditionalProperties(Map.of("currentUsage", updateUsageProperty));

		testThingRepository.updateProperty(URI.create("urn:ngsi-ld:crane:my-test-entity"), fragmentVO);
	}
}
