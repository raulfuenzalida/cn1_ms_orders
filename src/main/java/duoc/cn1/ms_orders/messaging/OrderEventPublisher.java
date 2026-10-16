package duoc.cn1.ms_orders.messaging;

import duoc.cn1.ms_orders.event.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventPublisher {

	private final RabbitTemplate rabbitTemplate;
	private final TopicExchange eventsExchange;

	@Value("${rabbitmq.routing-key.order-created}")
	private String orderCreatedRoutingKey;

	public void publishOrderCreated(OrderCreatedEvent event) {
		try {
			rabbitTemplate.convertAndSend(
				eventsExchange.getName(),
				orderCreatedRoutingKey,
				event
			);
			log.info("Evento order.created publicado: orderId={}, orderNumber={}",
				event.getOrderId(), event.getOrderNumber());
		} catch (Exception e) {
			log.error("Error al publicar evento order.created para orderId={}: {}",
				event.getOrderId(), e.getMessage(), e);
			throw e;
		}
	}
}
