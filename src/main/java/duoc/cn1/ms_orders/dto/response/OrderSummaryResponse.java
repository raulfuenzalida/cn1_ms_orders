package duoc.cn1.ms_orders.dto.response;

import duoc.cn1.ms_orders.model.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderSummaryResponse {

	private Long id;
	private String orderNumber;
	private String customerName;
	private String customerEmail;
	private OrderStatus status;
	private BigDecimal total;
	private LocalDateTime createdAt;
	private Integer itemCount;
}
