package duoc.cn1.ms_orders.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrderItemRequest {

	@NotNull(message = "El ID del producto es obligatorio")
	private Long idProduct;

	@NotNull(message = "La cantidad es obligatoria")
	@Min(value = 1, message = "La cantidad debe ser al menos 1")
	private Integer quantity;
}
