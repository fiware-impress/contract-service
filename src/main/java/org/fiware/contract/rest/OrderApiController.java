package org.fiware.contract.rest;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;
import lombok.RequiredArgsConstructor;
import org.fiware.contract.IdHelper;
import org.fiware.contract.api.OrderApi;
import org.fiware.contract.mapping.EntityMapper;
import org.fiware.contract.model.ItemAvailability;
import org.fiware.contract.model.Offer;
import org.fiware.contract.model.Order;
import org.fiware.contract.model.OrderVO;
import org.fiware.contract.repository.OfferRepository;
import org.fiware.contract.repository.OrderRepository;
import org.fiware.contract.repository.OrganizationRepository;
import org.fiware.contract.repository.PerseoRuleRepository;
import org.fiware.contract.service.OrderService;

import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Controller
public class OrderApiController implements OrderApi {

	private final ExecutorService executorService;
	private final EntityMapper entityMapper;
	private final OrderRepository orderRepository;
	private final OrganizationRepository organizationRepository;
	private final OfferRepository offerRepository;
	private final OrderService orderService;

	// required, since the mutliple calls to orion and  can block the event loop
	@ExecuteOn(TaskExecutors.IO)
	@Override
	public HttpResponse<Object> createOrder(OrderVO orderVO) {
		orderVO.setId(UUID.randomUUID().toString());
		Order order = entityMapper.orderVoToOrder(
				orderVO,
				organizationRepository,
				offerRepository);

		return orderService.createOrder(order)
				.map(uri -> HttpResponse.created(uri))
				.orElse(HttpResponse.created(HttpResponse.badRequest("Offer is not available.")));
	}

	// required, since the mutliple calls to orion and  can block the event loop
	@ExecuteOn(TaskExecutors.IO)
	@Override
	public List<OrderVO> getOrder() {
		return orderRepository.getOrders().stream().filter(Objects::nonNull).map(entityMapper::orderToOrderVo).collect(Collectors.toList());
	}

	// required, since the mutliple calls to orion and  can block the event loop
	@ExecuteOn(TaskExecutors.IO)
	@Override
	public Optional<OrderVO> getOrderById(String id) {
		return orderRepository.getOrderById(IdHelper.getUriFromId("order", id)).map(entityMapper::orderToOrderVo);

	}
}
