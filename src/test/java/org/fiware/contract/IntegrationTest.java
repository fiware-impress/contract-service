package org.fiware.contract;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import lombok.extern.slf4j.Slf4j;
import org.fiware.broker.api.SubscriptionsApiClient;
import org.fiware.contract.api.OfferApiTestClient;
import org.fiware.contract.api.OrderApiTestClient;
import org.fiware.contract.api.OrganizationApiTestClient;
import org.fiware.contract.api.SmartServiceApiTestClient;
import org.fiware.contract.configuration.GeneralProperties;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.HttpWaitStrategy;

import java.io.File;
import java.net.URI;
import java.time.Duration;
import java.time.temporal.ChronoUnit;

@Slf4j
@MicronautTest
public class IntegrationTest extends ApiTest {

	protected static final int ORION_LD_PORT = 1026;
	protected static final String ORION_LD_HOST = "orion-ld";
	protected static final int PERSEO_PORT = 9090;
	protected static final String PERSEO_HOST = "perseo-fe";

	private static final URI CORE_CONTEXT = URI.create("https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld");

	public IntegrationTest(TestThingRepository testThingRepository, SubscriptionsApiClient subscriptionsApiClient, GeneralProperties generalProperties, OfferApiTestClient offerApiTestClient, OrganizationApiTestClient organizationApiTestClient, SmartServiceApiTestClient serviceApiTestClient, OrderApiTestClient orderApiTestClient) {
		super(testThingRepository, subscriptionsApiClient, generalProperties, offerApiTestClient, organizationApiTestClient, serviceApiTestClient, orderApiTestClient);
	}

	private static final DockerComposeContainer DOCKER_COMPOSE_CONTAINER = new DockerComposeContainer(new File("env/docker-compose.yaml"))
			.withExposedService("orion-ld", ORION_LD_PORT)
			.withExposedService("perseo-fe", PERSEO_PORT);

	{
		DOCKER_COMPOSE_CONTAINER.waitingFor(ORION_LD_HOST, new OrionWaitStrategy()
				.withReadTimeout(Duration.of(10, ChronoUnit.MINUTES)).forPort(ORION_LD_PORT).forPath("/version"));
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
