package org.fiware.contract.repository;

import lombok.RequiredArgsConstructor;
import org.fiware.contract.IdHelper;
import org.fiware.contract.configuration.GeneralProperties;
import org.fiware.perseo.api.RulesApiClient;
import org.fiware.perseo.model.ActionParametersVO;
import org.fiware.perseo.model.HeadersVO;
import org.fiware.perseo.model.PostActionVO;
import org.fiware.perseo.model.RuleVO;

import javax.inject.Singleton;
import java.net.URI;

@Singleton
@RequiredArgsConstructor
public class PerseoRuleRepository {

	private static final String RULE_NAME_TEMPLATE = "%s_rule";
	private final GeneralProperties generalProperties;
	private final RulesApiClient rulesApiClient;

	public void createRule(String orderId, String priceDefinitionId, String expressionText) {
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
	}

}
