package org.fiware.contract.repository;

import lombok.RequiredArgsConstructor;
import org.fiware.broker.api.SubscriptionsApiClient;
import org.fiware.broker.model.EndpointReceiverInfoVO;
import org.fiware.broker.model.EndpointVO;
import org.fiware.broker.model.EntityInfoVO;
import org.fiware.broker.model.EntityVO;
import org.fiware.broker.model.NotificationParamsVO;
import org.fiware.broker.model.SubscriptionVO;
import org.fiware.contract.IdHelper;
import org.fiware.contract.configuration.GeneralProperties;
import org.fiware.perseo.api.RulesApiClient;
import org.fiware.perseo.model.ActionParametersVO;
import org.fiware.perseo.model.HeadersVO;
import org.fiware.perseo.model.PostActionVO;
import org.fiware.perseo.model.RuleVO;

import javax.inject.Singleton;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

@Singleton
@RequiredArgsConstructor
public class PerseoRuleRepository {

	private static final String RULE_NAME_TEMPLATE = "%s_rule";
	private final GeneralProperties generalProperties;
	private final RulesApiClient rulesApiClient;
	private final SubscriptionsApiClient subscriptionsApiClient;

	public void createRule(String orderId, String priceDefinitionId, String expressionText, URI measuredEntity) {
		HeadersVO headersVO = new HeadersVO();
		headersVO.put("Content-Type", "application/json");
		ActionParametersVO parametersVO = new ActionParametersVO()
				.url(generalProperties.getContractServiceCallbackUrl().toString())
				.headers(headersVO);
		PostActionVO postActionVO = new PostActionVO()
				.type(PostActionVO.Type.POST)
				.template("{ \"orderId\" : \"" + orderId + "\"," +
						"\"priceDefinitionId\": \"" + priceDefinitionId + "\"," +
						"\"currentValue\": \"${CurrentValue}\"" +
						"}")
				.parameters(parametersVO);
		RuleVO rule = new RuleVO()
				.name(String.format(RULE_NAME_TEMPLATE, IdHelper.getIdFromIdentifier(URI.create(priceDefinitionId))))
				.text(expressionText)
				.action(postActionVO);
		rulesApiClient.createRule(generalProperties.getTenant(), rule);
		try {
			createSubscription(measuredEntity);
		} catch (URISyntaxException e) {
			throw new RuntimeException("Was not able to create subscription", e);
		}
	}

	private void createSubscription(URI measuredEntity) throws URISyntaxException {
		EntityInfoVO entityInfoVO = new EntityInfoVO()
				.id(measuredEntity)
				.type(getType(measuredEntity));
		NotificationParamsVO notificationParamsVO = new NotificationParamsVO()
				.endpoint(new EndpointVO()
						.uri(generalProperties.getPerseoUrl().toURI())
						.addReceiverInfoItem(new EndpointReceiverInfoVO().key("Fiware-Service").value(generalProperties.getTenant()))
				);
		SubscriptionVO subscriptionVO = new SubscriptionVO()
				.atContext(generalProperties.getContextUrl())
				.type(SubscriptionVO.Type.SUBSCRIPTION)
				.entities(List.of(entityInfoVO))
				.geoQ(null)
				.notification(notificationParamsVO);
		subscriptionsApiClient.createSubscription(subscriptionVO, generalProperties.getTenant());
	}

	private String getType(URI measuredEntity) {
		String[] idParts = measuredEntity.toString().split(":");
		return idParts[2];
	}

}
