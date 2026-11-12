package duoc.cn1.ms_orders.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FilamentConsumptionEvent {

	public static final String EVENT_TYPE = "FILAMENT_CONSUMPTION";

	private String eventType;
	private Long orderId;
	private String orderNumber;
	private List<FilamentConsumption> filaments;
	private LocalDateTime timestamp;

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class FilamentConsumption {
		private Long filamentId;
		private BigDecimal grams;
	}
}
