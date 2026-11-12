package duoc.cn1.ms_orders.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import duoc.cn1.ms_orders.event.FilamentConsumptionEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaEventPublisher {

	private final KafkaTemplate<String, String> kafkaTemplate;
	private final ObjectMapper objectMapper;

	@Value("${kafka.topic.filament-consumption}")
	private String filamentConsumptionTopic;

	/**
	 * Publica el evento de consumo de filamento en Kafka.
	 * Si hay una transacción activa, espera al commit para no publicar
	 * un evento de una orden que aún no está persistida.
	 * La clave es el orderId para mantener el orden de eventos de una
	 * misma orden dentro de una partición.
	 */
	public void publishAfterCommit(FilamentConsumptionEvent event) {
		if (TransactionSynchronizationManager.isSynchronizationActive()) {
			TransactionSynchronizationManager.registerSynchronization(
				new TransactionSynchronization() {
					@Override
					public void afterCommit() {
						publish(event);
					}
				}
			);
		} else {
			publish(event);
		}
	}

	public void publish(FilamentConsumptionEvent event) {
		try {
			String json = objectMapper.writeValueAsString(event);

			kafkaTemplate.send(
				filamentConsumptionTopic,
				String.valueOf(event.getOrderId()),
				json
			).whenComplete((result, ex) -> {
				if (ex != null) {
					log.error("Error al publicar FILAMENT_CONSUMPTION en Kafka "
						+ "para orderId={}: {}", event.getOrderId(), ex.getMessage(), ex);
				} else {
					log.info("Evento FILAMENT_CONSUMPTION publicado en Kafka: "
						+ "orderId={}, orderNumber={}, particion={}, offset={}",
						event.getOrderId(), event.getOrderNumber(),
						result.getRecordMetadata().partition(),
						result.getRecordMetadata().offset());
				}
			});
		} catch (Exception e) {
			log.error("Error al publicar FILAMENT_CONSUMPTION en Kafka "
				+ "para orderId={}: {}", event.getOrderId(), e.getMessage(), e);
		}
	}
}
