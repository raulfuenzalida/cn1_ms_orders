package duoc.cn1.ms_orders.controller;

import duoc.cn1.ms_orders.dto.request.OrderCreateRequest;
import duoc.cn1.ms_orders.dto.response.OrderResponse;
import duoc.cn1.ms_orders.dto.response.OrderSummaryResponse;
import duoc.cn1.ms_orders.model.OrderStatus;
import duoc.cn1.ms_orders.service.OrderService;
import duoc.cn1.ms_orders.service.PdfService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Tag(name = "Pedidos", description = "API para gestión de pedidos")
@SecurityRequirement(name = "oauth2")
public class OrderController {

	private final OrderService orderService;
	private final PdfService pdfService;

	@GetMapping
	@Operation(summary = "Obtener todos los pedidos", description = "Retorna una lista con todos los pedidos registrados")
	public ResponseEntity<List<OrderSummaryResponse>> getAllOrders() {
		return ResponseEntity.ok(orderService.getAllOrders());
	}

	@GetMapping(params = "status")
	@Operation(summary = "Obtener pedidos por estado", description = "Retorna una lista de pedidos filtrados por estado")
	public ResponseEntity<List<OrderSummaryResponse>> getOrdersByStatus(@RequestParam OrderStatus status) {
		return ResponseEntity.ok(orderService.getOrdersByStatus(status));
	}

	@GetMapping(params = "customerName")
	@Operation(summary = "Obtener pedidos por nombre de cliente", description = "Retorna una lista de pedidos filtrados por nombre de cliente")
	public ResponseEntity<List<OrderSummaryResponse>> getOrdersByCustomerName(@RequestParam String customerName) {
		return ResponseEntity.ok(orderService.getOrdersByCustomerName(customerName));
	}

	@GetMapping(params = "customerEmail")
	@Operation(summary = "Obtener pedidos por email de cliente", description = "Retorna una lista de pedidos filtrados por email de cliente")
	public ResponseEntity<List<OrderSummaryResponse>> getOrdersByCustomerEmail(@RequestParam String customerEmail) {
		return ResponseEntity.ok(orderService.getOrdersByCustomerEmail(customerEmail));
	}

	@GetMapping("/{id}")
	@Operation(summary = "Obtener pedido por ID", description = "Retorna los detalles completos de un pedido específico")
	public ResponseEntity<OrderResponse> getOrderById(@PathVariable Long id) {
		return ResponseEntity.ok(orderService.getOrderById(id));
	}

	@PostMapping
	@Operation(summary = "Crear nuevo pedido", description = "Crea un nuevo pedido con los datos proporcionados")
	public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody OrderCreateRequest request) {
		OrderResponse response = orderService.createOrder(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@PostMapping("/{id}/confirm")
	@Operation(summary = "Confirmar pedido", description = "Confirma un pedido existente")
	public ResponseEntity<OrderResponse> confirmOrder(@PathVariable Long id) {
		return ResponseEntity.ok(orderService.confirmOrder(id));
	}

	@PostMapping("/{id}/complete")
	@Operation(summary = "Completar pedido", description = "Marca un pedido como completado")
	public ResponseEntity<OrderResponse> completeOrder(@PathVariable Long id) {
		return ResponseEntity.ok(orderService.completeOrder(id));
	}

	@PostMapping("/{id}/cancel")
	@Operation(summary = "Cancelar pedido", description = "Cancela un pedido existente")
	public ResponseEntity<OrderResponse> cancelOrder(@PathVariable Long id) {
		return ResponseEntity.ok(orderService.cancelOrder(id));
	}

	@GetMapping("/{id}/receipt")
	@Operation(summary = "Obtener comprobante PDF", description = "Genera y retorna el comprobante PDF de un pedido")
	public ResponseEntity<byte[]> getOrderReceipt(@PathVariable Long id) {
		byte[] pdf = pdfService.generateOrderReceipt(id);
		
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_PDF);
		headers.setContentDispositionFormData("attachment", "PrintWorks_PW-" + String.format("%06d", id) + ".pdf");
		
		return ResponseEntity.ok()
			.headers(headers)
			.body(pdf);
	}
}
