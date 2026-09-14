package duoc.cn1.ms_orders.service;

import duoc.cn1.ms_orders.client.ProductDto;
import duoc.cn1.ms_orders.client.ProductServiceClient;
import duoc.cn1.ms_orders.dto.request.OrderCreateRequest;
import duoc.cn1.ms_orders.dto.request.OrderItemRequest;
import duoc.cn1.ms_orders.exception.*;
import duoc.cn1.ms_orders.model.Order;
import duoc.cn1.ms_orders.model.OrderItem;
import duoc.cn1.ms_orders.model.OrderStatus;
import duoc.cn1.ms_orders.repository.OrderItemRepository;
import duoc.cn1.ms_orders.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private ProductServiceClient productServiceClient;

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
    void createOrder_ValidRequest_Success() {
        ProductDto product = new ProductDto();
        product.setId(1L);
        product.setName("Producto Test");
        product.setFinalPrice(new BigDecimal("2500"));

        when(productServiceClient.getProductById(1L))
            .thenReturn(product);

        when(orderRepository.save(any(Order.class)))
            .thenAnswer(invocation -> {
                Order order = invocation.getArgument(0);
                order.setId(1L);
                return order;
            });

        when(orderItemRepository.saveAll(anyList()))
            .thenAnswer(invocation -> {
                List<OrderItem> items = invocation.getArgument(0);

                for (int i = 0; i < items.size(); i++) {
                    items.get(i).setId((long) (i + 1));
                }

                return items;
            });

        var response = orderService.createOrder(validRequest);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Test Customer", response.getCustomerName());
        assertEquals("test@example.com", response.getCustomerEmail());
        assertEquals(OrderStatus.CREATED, response.getStatus());
        assertEquals(new BigDecimal("5000"), response.getTotal());

        assertNotNull(response.getItems());
        assertEquals(1, response.getItems().size());

        var item = response.getItems().get(0);

        assertEquals(1L, item.getIdProduct());
        assertEquals("Producto Test", item.getProductName());
        assertEquals(new BigDecimal("2500"), item.getUnitPrice());
        assertEquals(2, item.getQuantity());
        assertEquals(new BigDecimal("5000"), item.getSubtotal());

        verify(productServiceClient).getProductById(1L);
        verify(orderRepository).save(any(Order.class));
        verify(orderItemRepository).saveAll(anyList());
    }

    @Test
    void createOrder_EmptyItems_ThrowsException() {
        validRequest.setItems(Collections.emptyList());

        assertThrows(
            InvalidCustomerDataException.class,
            () -> orderService.createOrder(validRequest)
        );

        verifyNoInteractions(productServiceClient);
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void createOrder_TooManyItems_ThrowsException() {
        List<OrderItemRequest> items =
            Collections.nCopies(31, new OrderItemRequest());

        validRequest.setItems(items);

        assertThrows(
            TooManyDistinctProductsException.class,
            () -> orderService.createOrder(validRequest)
        );

        verifyNoInteractions(productServiceClient);
        verify(orderRepository, never()).save(any(Order.class));
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

        assertThrows(
            DuplicateProductException.class,
            () -> orderService.createOrder(validRequest)
        );

        verifyNoInteractions(productServiceClient);
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void createOrder_InvalidQuantity_ThrowsException() {
        validRequest.getItems().get(0).setQuantity(0);

        ProductDto product = new ProductDto();
        product.setId(1L);
        product.setName("Producto Test");
        product.setFinalPrice(new BigDecimal("2500"));

        when(productServiceClient.getProductById(1L))
            .thenReturn(product);

        assertThrows(
            InvalidQuantityException.class,
            () -> orderService.createOrder(validRequest)
        );

        verify(orderRepository, never()).save(any(Order.class));
        verify(orderItemRepository, never()).saveAll(anyList());
    }

    @Test
    void confirmOrder_ValidTransition_Success() {
        Order order = createOrderWithStatus(OrderStatus.CREATED);

        when(orderRepository.findById(1L))
            .thenReturn(Optional.of(order));

        when(orderRepository.save(any(Order.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        var response = orderService.confirmOrder(1L);

        assertEquals(OrderStatus.CONFIRMED, response.getStatus());
        assertNotNull(response.getConfirmedAt());

        verify(orderRepository).save(order);
    }

    @Test
    void confirmOrder_InvalidTransition_ThrowsException() {
        Order order = createOrderWithStatus(OrderStatus.COMPLETED);

        when(orderRepository.findById(1L))
            .thenReturn(Optional.of(order));

        assertThrows(
            InvalidOrderTransitionException.class,
            () -> orderService.confirmOrder(1L)
        );

        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void completeOrder_ValidTransition_Success() {
        Order order = createOrderWithStatus(OrderStatus.CONFIRMED);

        when(orderRepository.findById(1L))
            .thenReturn(Optional.of(order));

        when(orderRepository.save(any(Order.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        var response = orderService.completeOrder(1L);

        assertEquals(OrderStatus.COMPLETED, response.getStatus());
        assertNotNull(response.getCompletedAt());

        verify(orderRepository).save(order);
    }

    @Test
    void cancelOrder_ValidTransition_Success() {
        Order order = createOrderWithStatus(OrderStatus.CREATED);

        when(orderRepository.findById(1L))
            .thenReturn(Optional.of(order));

        when(orderRepository.save(any(Order.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        var response = orderService.cancelOrder(1L);

        assertEquals(OrderStatus.CANCELLED, response.getStatus());
        assertNotNull(response.getCancelledAt());

        verify(orderRepository).save(order);
    }

    @Test
    void cancelOrder_CompletedOrder_ThrowsException() {
        Order order = createOrderWithStatus(OrderStatus.COMPLETED);

        when(orderRepository.findById(1L))
            .thenReturn(Optional.of(order));

        assertThrows(
            InvalidOrderTransitionException.class,
            () -> orderService.cancelOrder(1L)
        );

        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void getOrderById_ExistingOrder_Success() {
        Order order = createOrderWithStatus(OrderStatus.CREATED);

        when(orderRepository.findById(1L))
            .thenReturn(Optional.of(order));

        var response = orderService.getOrderById(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Test Customer", response.getCustomerName());
        assertEquals("test@example.com", response.getCustomerEmail());
        assertEquals(OrderStatus.CREATED, response.getStatus());
    }

    @Test
    void getOrderById_NonExistingOrder_ThrowsException() {
        when(orderRepository.findById(1L))
            .thenReturn(Optional.empty());

        assertThrows(
            OrderNotFoundException.class,
            () -> orderService.getOrderById(1L)
        );
    }

    private Order createOrderWithStatus(OrderStatus status) {
        Order order = new Order();

        order.setId(1L);
        order.setCustomerName("Test Customer");
        order.setCustomerEmail("test@example.com");
        order.setStatus(status);
        order.setTotal(new BigDecimal("200.00"));
        order.setItems(Collections.emptyList());

        return order;
    }
}