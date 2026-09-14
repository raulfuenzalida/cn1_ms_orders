package duoc.cn1.ms_orders.service;

import duoc.cn1.ms_orders.dto.request.OrderCreateRequest;
import duoc.cn1.ms_orders.dto.request.OrderItemRequest;
import duoc.cn1.ms_orders.exception.*;
import duoc.cn1.ms_orders.model.Order;
import duoc.cn1.ms_orders.model.OrderStatus;
import duoc.cn1.ms_orders.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

	@Mock
	private OrderRepository orderRepository;

	@InjectMocks
	private OrderService orderService;

	private OrderCreateRequest validRequest;

	@BeforeEach
	void setUp() {
		OrderItemRequest itemRequest = new OrderItemRequest();
		itemRequest.setIdProduct(1L);
		itemRequest.setQuantity(2);

		validRequest = new OrderCreateRequest();
		validRequest.setCustomerName("Test Customer");
		validRequest.setCustomerEmail("test@example.com");
		validRequest.setItems(Collections.singletonList(itemRequest));
	}

	@Test
	void createOrder_EmptyItems_ThrowsException() {
		validRequest.setItems(Collections.emptyList());
		assertThrows(InvalidCustomerDataException.class, () -> orderService.createOrder(validRequest));
	}

	@Test
	void createOrder_TooManyItems_ThrowsException() {
		List<OrderItemRequest> items = Collections.nCopies(31, new OrderItemRequest());
		validRequest.setItems(items);
		assertThrows(TooManyDistinctProductsException.class, () -> orderService.createOrder(validRequest));
	}

	@Test
	void createOrder_DuplicateProducts_ThrowsException() {
		OrderItemRequest item1 = new OrderItemRequest();
		item1.setIdProduct(1L);
		item1.setQuantity(2);

		OrderItemRequest item2 = new OrderItemRequest();
		item2.setIdProduct(1L);
		item2.setQuantity(3);

		validRequest.setItems(Arrays.asList(item1, item2));
		assertThrows(DuplicateProductException.class, () -> orderService.createOrder(validRequest));
	}

	@Test
	void confirmOrder_ValidTransition_Success() {
		Order order = new Order();
		order.setId(1L);
		order.setStatus(OrderStatus.CREATED);

		when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
		when(orderRepository.save(any(Order.class))).thenReturn(order);

		var response = orderService.confirmOrder(1L);

		assertEquals(OrderStatus.CONFIRMED, response.getStatus());
		assertNotNull(response.getConfirmedAt());
	}

	@Test
	void confirmOrder_InvalidTransition_ThrowsException() {
		Order order = new Order();
		order.setId(1L);
		order.setStatus(OrderStatus.COMPLETED);

		when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
		assertThrows(InvalidOrderTransitionException.class, () -> orderService.confirmOrder(1L));
	}

	@Test
	void completeOrder_ValidTransition_Success() {
		Order order = new Order();
		order.setId(1L);
		order.setStatus(OrderStatus.CONFIRMED);

		when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
		when(orderRepository.save(any(Order.class))).thenReturn(order);

		var response = orderService.completeOrder(1L);

		assertEquals(OrderStatus.COMPLETED, response.getStatus());
		assertNotNull(response.getCompletedAt());
	}

	@Test
	void cancelOrder_ValidTransition_Success() {
		Order order = new Order();
		order.setId(1L);
		order.setStatus(OrderStatus.CREATED);

		when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
		when(orderRepository.save(any(Order.class))).thenReturn(order);

		var response = orderService.cancelOrder(1L);

		assertEquals(OrderStatus.CANCELLED, response.getStatus());
		assertNotNull(response.getCancelledAt());
	}

	@Test
	void cancelOrder_CompletedOrder_ThrowsException() {
		Order order = new Order();
		order.setId(1L);
		order.setStatus(OrderStatus.COMPLETED);

		when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
		assertThrows(InvalidOrderTransitionException.class, () -> orderService.cancelOrder(1L));
	}

	@Test
	void getOrderById_ExistingOrder_Success() {
		Order order = new Order();
		order.setId(1L);
		order.setCustomerName("Test Customer");
		order.setCustomerEmail("test@example.com");
		order.setStatus(OrderStatus.CREATED);
		order.setTotal(new java.math.BigDecimal("200.00"));

		when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

		var response = orderService.getOrderById(1L);

		assertNotNull(response);
		assertEquals(1L, response.getId());
		assertEquals("Test Customer", response.getCustomerName());
	}

	@Test
	void getOrderById_NonExistingOrder_ThrowsException() {
		when(orderRepository.findById(1L)).thenReturn(Optional.empty());
		assertThrows(OrderNotFoundException.class, () -> orderService.getOrderById(1L));
	}
}
