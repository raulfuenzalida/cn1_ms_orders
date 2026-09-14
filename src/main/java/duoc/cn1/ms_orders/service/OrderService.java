package duoc.cn1.ms_orders.service;

import duoc.cn1.ms_orders.client.ProductDto;
import duoc.cn1.ms_orders.client.ProductServiceClient;
import duoc.cn1.ms_orders.dto.request.OrderCreateRequest;
import duoc.cn1.ms_orders.dto.request.OrderItemRequest;
import duoc.cn1.ms_orders.dto.response.OrderItemResponse;
import duoc.cn1.ms_orders.dto.response.OrderResponse;
import duoc.cn1.ms_orders.dto.response.OrderSummaryResponse;
import duoc.cn1.ms_orders.exception.*;
import duoc.cn1.ms_orders.model.Order;
import duoc.cn1.ms_orders.model.OrderItem;
import duoc.cn1.ms_orders.model.OrderStatus;
import duoc.cn1.ms_orders.repository.OrderItemRepository;
import duoc.cn1.ms_orders.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductServiceClient productServiceClient;

    public List<OrderSummaryResponse> getAllOrders() {
        return orderRepository.findAll().stream()
            .map(this::mapToSummaryResponse)
            .collect(Collectors.toList());
    }

    public List<OrderSummaryResponse> getOrdersByStatus(OrderStatus status) {
        return orderRepository.findByStatus(status).stream()
            .map(this::mapToSummaryResponse)
            .collect(Collectors.toList());
    }

    public List<OrderSummaryResponse> getOrdersByCustomerName(String customerName) {
        return orderRepository.findByCustomerNameContainingIgnoreCase(customerName).stream()
            .map(this::mapToSummaryResponse)
            .collect(Collectors.toList());
    }

    public List<OrderSummaryResponse> getOrdersByCustomerEmail(String customerEmail) {
        return orderRepository.findByCustomerEmailContainingIgnoreCase(customerEmail).stream()
            .map(this::mapToSummaryResponse)
            .collect(Collectors.toList());
    }

    public OrderResponse getOrderById(Long id) {
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new OrderNotFoundException(id));

        return mapToResponse(order);
    }

    public OrderResponse createOrder(OrderCreateRequest request) {
        validateOrderRequest(request);

        List<OrderItem> items = request.getItems().stream()
            .map(itemRequest -> {
                ProductDto product =
                    productServiceClient.getProductById(
                        itemRequest.getIdProduct()
                    );

                validateQuantity(itemRequest.getQuantity());

                BigDecimal unitPrice = product.getFinalPrice();

                BigDecimal subtotal = unitPrice.multiply(
                    BigDecimal.valueOf(itemRequest.getQuantity())
                );

                return new OrderItem(
                    null,
                    itemRequest.getIdProduct(),
                    product.getName(),
                    unitPrice,
                    itemRequest.getQuantity(),
                    subtotal
                );
            })
            .collect(Collectors.toList());

        BigDecimal total = items.stream()
            .map(OrderItem::getSubtotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        Order order = new Order(
            request.getCustomerName(),
            request.getCustomerEmail(),
            OrderStatus.CREATED,
            total
        );

        Order savedOrder = orderRepository.save(order);

        items.forEach(item -> item.setOrder(savedOrder));

        List<OrderItem> savedItems =
            orderItemRepository.saveAll(items);

        savedOrder.setItems(savedItems);

        log.info(
            "Pedido creado exitosamente: ID {}, Cliente: {}, Total: {}",
            savedOrder.getId(),
            savedOrder.getCustomerName(),
            savedOrder.getTotal()
        );

        return mapToResponse(savedOrder);
    }

    public OrderResponse confirmOrder(Long id) {
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new OrderNotFoundException(id));

        validateTransition(
            order.getStatus(),
            OrderStatus.CONFIRMED
        );

        order.setStatus(OrderStatus.CONFIRMED);
        order.setConfirmedAt(LocalDateTime.now());

        Order updatedOrder = orderRepository.save(order);

        log.info("Pedido confirmado: ID {}", id);

        return mapToResponse(updatedOrder);
    }

    public OrderResponse completeOrder(Long id) {
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new OrderNotFoundException(id));

        validateTransition(
            order.getStatus(),
            OrderStatus.COMPLETED
        );

        order.setStatus(OrderStatus.COMPLETED);
        order.setCompletedAt(LocalDateTime.now());

        Order updatedOrder = orderRepository.save(order);

        log.info("Pedido completado: ID {}", id);

        return mapToResponse(updatedOrder);
    }

    public OrderResponse cancelOrder(Long id) {
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new OrderNotFoundException(id));

        validateTransition(
            order.getStatus(),
            OrderStatus.CANCELLED
        );

        order.setStatus(OrderStatus.CANCELLED);
        order.setCancelledAt(LocalDateTime.now());

        Order updatedOrder = orderRepository.save(order);

        log.info("Pedido cancelado: ID {}", id);

        return mapToResponse(updatedOrder);
    }

    private void validateOrderRequest(OrderCreateRequest request) {
        if (
            request.getItems() == null ||
            request.getItems().isEmpty()
        ) {
            throw new InvalidCustomerDataException(
                "El pedido debe tener al menos un producto"
            );
        }

        if (request.getItems().size() > 30) {
            throw new TooManyDistinctProductsException(
                request.getItems().size()
            );
        }

        Set<Long> productIds = request.getItems().stream()
            .map(OrderItemRequest::getIdProduct)
            .collect(Collectors.toSet());

        if (productIds.size() != request.getItems().size()) {
            throw new DuplicateProductException(
                "El pedido contiene productos duplicados"
            );
        }
    }

    private void validateQuantity(Integer quantity) {
        if (
            quantity == null ||
            quantity < 1 ||
            quantity > 99
        ) {
            throw new InvalidQuantityException(quantity);
        }
    }

    private void validateTransition(
            OrderStatus currentStatus,
            OrderStatus targetStatus) {

        boolean validTransition = switch (currentStatus) {
            case CREATED ->
                targetStatus == OrderStatus.CONFIRMED ||
                targetStatus == OrderStatus.CANCELLED;

            case CONFIRMED ->
                targetStatus == OrderStatus.COMPLETED ||
                targetStatus == OrderStatus.CANCELLED;

            case COMPLETED, CANCELLED -> false;
        };

        if (!validTransition) {
            throw new InvalidOrderTransitionException(
                currentStatus.name(),
                targetStatus.name()
            );
        }
    }

    private OrderResponse mapToResponse(Order order) {
        List<OrderItemResponse> itemResponses =
            order.getItems().stream()
                .map(this::mapToItemResponse)
                .collect(Collectors.toList());

        return new OrderResponse(
            order.getId(),
            order.getOrderNumber(),
            order.getCustomerName(),
            order.getCustomerEmail(),
            order.getStatus(),
            order.getTotal(),
            order.getCreatedAt(),
            order.getUpdatedAt(),
            order.getConfirmedAt(),
            order.getCompletedAt(),
            order.getCancelledAt(),
            itemResponses
        );
    }

    private OrderItemResponse mapToItemResponse(OrderItem item) {
        return new OrderItemResponse(
            item.getId(),
            item.getIdProduct(),
            item.getProductName(),
            item.getUnitPrice(),
            item.getQuantity(),
            item.getSubtotal()
        );
    }

    private OrderSummaryResponse mapToSummaryResponse(Order order) {
        return new OrderSummaryResponse(
            order.getId(),
            order.getOrderNumber(),
            order.getCustomerName(),
            order.getCustomerEmail(),
            order.getStatus(),
            order.getTotal(),
            order.getCreatedAt(),
            order.getItems().size()
        );
    }
}