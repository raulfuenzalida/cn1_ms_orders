package duoc.cn1.ms_orders.dto.response;

import duoc.cn1.ms_orders.model.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {

	private Long id;
	private String orderNumber;
	private String customerName;
	private String customerEmail;
	private OrderStatus status;
	private BigDecimal total;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	private LocalDateTime confirmedAt;
	private LocalDateTime completedAt;
	private LocalDateTime cancelledAt;
	private List<OrderItemResponse> items;
}
