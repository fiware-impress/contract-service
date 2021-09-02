package org.fiware.contract;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fiware.broker.api.SubscriptionsApiClient;
import org.fiware.broker.model.EndpointVO;
import org.fiware.broker.model.EntityFragmentVO;
import org.fiware.broker.model.EntityInfoVO;
import org.fiware.broker.model.EntityVO;
import org.fiware.broker.model.NotificationParamsVO;
import org.fiware.broker.model.PropertyVO;
import org.fiware.broker.model.SubscriptionVO;
import org.fiware.contract.configuration.GeneralProperties;
import org.fiware.contract.model.AddressVO;
import org.fiware.contract.model.MeasurementPointVO;
import org.fiware.contract.model.OfferVO;
import org.fiware.contract.model.OrderVO;
import org.fiware.contract.model.OrganizationVO;
import org.fiware.contract.model.PaymentMethod;
import org.fiware.contract.model.PriceDefinitionVO;
import org.fiware.contract.model.ProviderVO;
import org.fiware.contract.model.SmartServiceVO;
import org.fiware.contract.rest.OfferApiController;
import org.fiware.contract.rest.OrderApiController;
import org.fiware.contract.rest.OrganizationApiController;
import org.fiware.contract.rest.ServiceApiController;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.HttpWaitStrategy;

import javax.inject.Inject;
import java.io.File;
import java.net.URI;
import java.time.Clock;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@MicronautTest
@RequiredArgsConstructor
public class IntegrationTest {

	protected static final int ORION_LD_PORT = 1026;
	protected static final String ORION_LD_HOST = "orion-ld";
	protected static final int PERSEO_PORT = 9090;
	protected static final String PERSEO_HOST = "perseo-fe";

	private static final URI CORE_CONTEXT = URI.create("https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld");

	private final OrganizationApiController organizationApiController;
	private final OfferApiController offerApiController;
	private final ServiceApiController serviceApiController;
	private final OrderApiController orderApiController;
	private final TestThingRepository testThingRepository;
	private final SubscriptionsApiClient subscriptionsApiClient;
	private final GeneralProperties generalProperties;


	private static final DockerComposeContainer DOCKER_COMPOSE_CONTAINER = new DockerComposeContainer(new File("env/docker-compose.yaml"))
			.withExposedService("orion-ld", ORION_LD_PORT)
			.withExposedService("perseo-fe", PERSEO_PORT);

	{
		DOCKER_COMPOSE_CONTAINER.waitingFor(ORION_LD_HOST, new OrionWaitStrategy()
				.withReadTimeout(Duration.of(10, ChronoUnit.MINUTES)).forPort(ORION_LD_PORT).forPath("/version"));
	}

	@Test
	public void test() {
		HttpResponse<Object> organizationCreationResponse = organizationApiController.createOrganization(
				new OrganizationVO()
						.addressCountry("Germany")
						.addressRegion("Saxony")
						.addressLocality("Dresden")
						.legalName("MyTestCompany GmbH")
						.email("test@company.org")
						.telephone("0351/1234567")
						.contactType("Support")
						.postalCode("01159")
						.streetAddress("Bergstraße 12"));

		assertEquals(HttpStatus.CREATED, organizationCreationResponse.getStatus(), "The organization should have been created.");
		String locationHeaderOrg = organizationCreationResponse.getHeaders().get("Location");
		assertNotNull(locationHeaderOrg, "A location header should have been returned.");
		assertFalse(locationHeaderOrg.isEmpty(), "The location header should contain an id.");

		HttpResponse<Object> customerCreationResponse = organizationApiController.createOrganization(
				new OrganizationVO()
						.addressCountry("Germany")
						.addressRegion("Saxony")
						.addressLocality("Dresden")
						.legalName("Customer Company")
						.email("customer@company.org")
						.telephone("0351/7654321")
						.contactType("Sales")
						.postalCode("01159")
						.streetAddress("Klingenbergerstr. 24"));

		assertEquals(HttpStatus.CREATED, customerCreationResponse.getStatus(), "The organization should have been created.");
		String locationHeaderCustomer = customerCreationResponse.getHeaders().get("Location");
		assertNotNull(locationHeaderCustomer, "A location header should have been returned.");
		assertFalse(locationHeaderCustomer.isEmpty(), "The location header should contain an id.");


		HttpResponse<Object> serviceCreationResponse = serviceApiController.createService(new SmartServiceVO()
				.serviceType("CraneUsage")
				.category("ConstructionArea")
				.priceDefinitions(List.of(
						new PriceDefinitionVO()
								.price(1.0)
								.unitCode("kg/m")
								.priceCurrency("Euro")
								.quantity(1000.0)
								.measurementPoint(new MeasurementPointVO()
										.property("usageinformation:currentLifting")
										.unitCode("kg/m")
										.provider(new ProviderVO()
												.id("myCrane")
												.type("Crane")))))
		);

		assertEquals(HttpStatus.CREATED, serviceCreationResponse.getStatus(), "The service should have been created.");
		String locationHeaderService = serviceCreationResponse.getHeaders().get("Location");
		assertNotNull(locationHeaderService, "A location header should have been returned.");
		assertFalse(locationHeaderService.isEmpty(), "The location header should contain an id.");

		HttpResponse<Object> offerCreationResponse = offerApiController.createOffer(
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

		HttpResponse<Object> orderCreationResponse = orderApiController.createOrder(new OrderVO()
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

		HttpResponse<Object> subscriptionCreationResponse = subscriptionsApiClient.createSubscription(generalProperties.getTenant(), new SubscriptionVO()
				.atContext(CORE_CONTEXT)
				.type(SubscriptionVO.Type.SUBSCRIPTION)
				.entities(List.of(new EntityInfoVO().type("Crane")))
				.geoQ(null)
				.notification(new NotificationParamsVO()
						.endpoint(new EndpointVO()
								.uri(URI.create("http://perseo-fe:9090/notices"))
						)
						)
		);
		assertEquals(HttpStatus.CREATED, subscriptionCreationResponse.getStatus(), "The subscription should have been created.");

		PropertyVO usageProperty = new PropertyVO()
				.type(PropertyVO.Type.PROPERTY)
				.value(10);
		EntityVO testEntity = new EntityVO()
				.atContext(CORE_CONTEXT)
				.id(URI.create("urn:ngsi-ld:crane:my-test-entity"))
				.type("Crane")
				.location(null)
				.observationSpace(null)
				.operationSpace(null)
				.setAdditionalProperties("currentUsage", usageProperty);

		testThingRepository.createThing(testEntity);

		updatePropertyForTestThing(20);
		updatePropertyForTestThing(	30);
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
				.setAdditionalProperties("currentUsage", updateUsageProperty);

		testThingRepository.updateProperty(URI.create("urn:ngsi-ld:crane:my-test-entity"), fragmentVO);
	}


	@BeforeAll
	public static void setupContainers() {
		DOCKER_COMPOSE_CONTAINER.start();
	}

	@AfterAll
	public static void tearDown() {
		DOCKER_COMPOSE_CONTAINER.stop();
	}

	// wait strategy for orion. Will wait and repeat after the first successful check to ensure its stable.
	class OrionWaitStrategy extends HttpWaitStrategy {

		private final Logger logger = LoggerFactory.getLogger(OrionWaitStrategy.class);


		public static final int RETEST_WAIT_IN_MS = 30000;

		@Override
		protected void waitUntilReady() {
			super.waitUntilReady();
			try {
				Thread.sleep(RETEST_WAIT_IN_MS);
			} catch (InterruptedException e) {
				logger.info("Sleep interrupted.");
			}
			super.waitUntilReady();
		}
	}
}
