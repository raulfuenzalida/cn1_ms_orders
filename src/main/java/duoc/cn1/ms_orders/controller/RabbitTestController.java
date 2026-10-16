package duoc.cn1.ms_orders.controller;

import duoc.cn1.ms_orders.event.OrderCreatedEvent;
import duoc.cn1.ms_orders.messaging.OrderEventPublisher;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/orders/test")
@RequiredArgsConstructor
@Tag(name = "RabbitMQ DEV", description = "Endpoints de prueba para RabbitMQ (solo desarrollo)")
@Profile("dev")
public class RabbitTestController {

	private final OrderEventPublisher orderEventPublisher;

	@PostMapping("/rabbitmq")
	@Operation(summary = "Publicar evento order.created de prueba", description = "Publica un evento de prueba en RabbitMQ para verificar la integración")
	public ResponseEntity<String> publishTestEvent(@RequestBody TestOrderRequest request) {
		OrderCreatedEvent event = OrderCreatedEvent.builder()
			.orderId(request.orderId())
			.orderNumber(request.orderNumber())
			.customerName(request.customerName())
			.customerEmail(request.customerEmail())
			.total(request.total())
			.status(request.status())
			.createdAt(LocalDateTime.now())
			.pdfBase64("")
			.build();

		orderEventPublisher.publishOrderCreated(event);

		return ResponseEntity.ok("Evento order.created publicado correctamente: " + request.orderNumber());
	}

	record TestOrderRequest(
		Long orderId,
		String orderNumber,
		String customerName,
		String customerEmail,
		BigDecimal total,
		String status
	) {}
}
