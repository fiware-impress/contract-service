package org.fiware.contract.rest;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import lombok.RequiredArgsConstructor;
import org.fiware.contract.IdHelper;
import org.fiware.contract.api.OrderApi;
import org.fiware.contract.mapping.EntityMapper;
import org.fiware.contract.model.Order;
import org.fiware.contract.model.OrderVO;
import org.fiware.contract.repository.OfferRepository;
import org.fiware.contract.repository.OrderRepository;
import org.fiware.contract.repository.OrganizationRepository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Controller
public class OrderApiController implements OrderApi {

	private final EntityMapper entityMapper;
	private final OrderRepository orderRepository;
	private final OrganizationRepository organizationRepository;
	private final OfferRepository offerRepository;

	@Override
	public HttpResponse<Object> createOrder(OrderVO orderVO) {
		orderVO.setId(UUID.randomUUID().toString());
		Order order = entityMapper.orderVoToOrder(
				orderVO,
				organizationRepository,
				offerRepository);
		return HttpResponse.created(orderRepository.createOrder(order));
	}

	@Override
	public List<OrderVO> getOrder() {
		return orderRepository.getOrders().stream().filter(Objects::nonNull).map(entityMapper::orderToOrderVo).collect(Collectors.toList());
	}

	@Override
	public Optional<OrderVO> getOrderById(String id) {
		return orderRepository.getOrderById(IdHelper.getUriFromId("order", id)).map(entityMapper::orderToOrderVo);

	}
}
