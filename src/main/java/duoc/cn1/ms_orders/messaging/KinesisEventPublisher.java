package duoc.cn1.ms_orders.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import duoc.cn1.ms_orders.event.FilamentConsumptionEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.kinesis.KinesisClient;
import software.amazon.awssdk.services.kinesis.model.PutRecordRequest;

import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RequiredArgsConstructor
public class KinesisEventPublisher {

	private final KinesisClient kinesisClient;
	private final ObjectMapper objectMapper;

	@Value("${kinesis.stream.filament-consumption}")
	private String filamentConsumptionStream;

	/**
	 * Publica el evento de consumo de filamento en Kinesis.
	 * Si hay una transacción activa, espera al commit para no publicar
	 * un evento de una orden que aún no está persistida.
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

			PutRecordRequest request = PutRecordRequest.builder()
				.streamName(filamentConsumptionStream)
				.partitionKey(String.valueOf(event.getOrderId()))
				.data(SdkBytes.fromString(json, StandardCharsets.UTF_8))
				.build();

			kinesisClient.putRecord(request);

			log.info("Evento FILAMENT_CONSUMPTION publicado en Kinesis: "
				+ "orderId={}, orderNumber={}, filamentos={}",
				event.getOrderId(), event.getOrderNumber(),
				event.getFilaments().size());
		} catch (Exception e) {
			log.error("Error al publicar FILAMENT_CONSUMPTION en Kinesis "
				+ "para orderId={}: {}", event.getOrderId(), e.getMessage(), e);
			throw new RuntimeException(
				"Error publicando evento de consumo de filamento en Kinesis", e);
		}
	}
}
