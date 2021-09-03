package org.fiware.contract;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.fiware.broker.api.SubscriptionsApiClient;
import org.fiware.contract.api.OfferApiTestClient;
import org.fiware.contract.api.OrderApiTestClient;
import org.fiware.contract.api.OrganizationApiTestClient;
import org.fiware.contract.api.SmartServiceApiTestClient;
import org.fiware.contract.configuration.GeneralProperties;

@MicronautTest(environments = "docker")
public class DockerTest extends ApiTest{

	public DockerTest(TestThingRepository testThingRepository, SubscriptionsApiClient subscriptionsApiClient, GeneralProperties generalProperties, OfferApiTestClient offerApiTestClient, OrganizationApiTestClient organizationApiTestClient, SmartServiceApiTestClient serviceApiTestClient, OrderApiTestClient orderApiTestClient) {
		super(testThingRepository, subscriptionsApiClient, generalProperties, offerApiTestClient, organizationApiTestClient, serviceApiTestClient, orderApiTestClient);
	}
}
