package duoc.cn1.ms_orders.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class OrderCreateRequest {

	@NotBlank(message = "El nombre del cliente es obligatorio")
	@Size(max = 200, message = "El nombre del cliente no puede exceder 200 caracteres")
	private String customerName;

	@NotBlank(message = "El email del cliente es obligatorio")
	@Email(message = "El email debe ser válido")
	@Size(max = 200, message = "El email no puede exceder 200 caracteres")
	private String customerEmail;

	@NotEmpty(message = "El pedido debe tener al menos un producto")
	@Size(min = 1, max = 30, message = "El pedido debe tener entre 1 y 30 productos distintos")
	@Valid
	private List<OrderItemRequest> items;
}
