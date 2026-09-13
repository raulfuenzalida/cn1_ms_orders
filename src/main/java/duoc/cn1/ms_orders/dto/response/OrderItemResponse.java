package duoc.cn1.ms_orders.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemResponse {

	private Long id;
	private Long idProduct;
	private String productName;
	private BigDecimal unitPrice;
	private Integer quantity;
	private BigDecimal subtotal;
}
