package org.fiware.contract.repository;

import io.micronaut.http.HttpResponse;
import lombok.extern.slf4j.Slf4j;
import org.fiware.broker.api.EntitiesApiClient;
import org.fiware.broker.model.EntityListVO;
import org.fiware.broker.model.EntityVO;
import org.fiware.contract.IdHelper;
import org.fiware.contract.configuration.GeneralProperties;
import org.fiware.contract.mapping.EntityMapper;
import org.fiware.contract.model.Invoice;

import javax.inject.Singleton;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Singleton
public class InvoiceRepository extends BrokerBaseRepository {

	private final OrganizationRepository organizationRepository;
	private final OrderRepository orderRepository;

	public InvoiceRepository(GeneralProperties generalProperties, EntityMapper entityMapper, EntitiesApiClient entitiesApi, OrganizationRepository organizationRepository, OrderRepository orderRepository) {
		super(generalProperties, entityMapper, entitiesApi);
		this.organizationRepository = organizationRepository;
		this.orderRepository = orderRepository;
	}

	public URI createInvoice(Invoice invoice) {
		EntityVO invoiceEntity = entityMapper.invoiceToEntityVO(generalProperties.getContextUrl(), invoice);
		HttpResponse<Object> response = entitiesApi.createEntity(generalProperties.getTenant(), invoiceEntity);
		return URI.create(IdHelper.getIdFromIdentifier(URI.create(response.getHeaders().get("Location"))));
	}

	public List<Invoice> getInvoices() {
		List<Invoice> invoiceList = new ArrayList<>();
		Optional<EntityListVO> optionalEntityVOS = entitiesApi.queryEntities(generalProperties.getTenant(), null, null, "Invoice", null, null, null, null, null, null, null, null, null, getLinkHeader()).getBody();
		if (optionalEntityVOS.isEmpty()) {
			return List.of();
		}
		for (EntityVO entityVO : optionalEntityVOS.get()) {
			invoiceList.add(entityMapper.entityVoToInvoice(entityVO, organizationRepository, orderRepository));
		}
		return invoiceList;
	}

	public Optional<Invoice> getInvoice(URI invoiceId) {
		return entitiesApi
				.retrieveEntityById(generalProperties.getTenant(), invoiceId, null, null, null, getLinkHeader())
				.getBody()
				.map(entityVO -> entityMapper.entityVoToInvoice(entityVO, organizationRepository, orderRepository));
	}
}
