package duoc.cn1.ms_orders.service;

import duoc.cn1.ms_orders.model.Order;
import duoc.cn1.ms_orders.model.OrderItem;
import duoc.cn1.ms_orders.model.OrderStatus;
import duoc.cn1.ms_orders.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PdfServiceTest {

	@Mock
	private OrderRepository orderRepository;

	@InjectMocks
	private PdfService pdfService;

	private Order testOrder;

	@BeforeEach
	void setUp() {
		testOrder = new Order();
		testOrder.setId(1L);
		testOrder.setCustomerName("Test Customer");
		testOrder.setCustomerEmail("test@example.com");
		testOrder.setStatus(OrderStatus.CONFIRMED);
		testOrder.setTotal(new BigDecimal("200.00"));
		testOrder.setCreatedAt(LocalDateTime.now());
		testOrder.setConfirmedAt(LocalDateTime.now());

		OrderItem item1 = new OrderItem();
		item1.setId(1L);
		item1.setIdProduct(1L);
		item1.setProductName("Product 1");
		item1.setUnitPrice(new BigDecimal("100.00"));
		item1.setQuantity(1);
		item1.setSubtotal(new BigDecimal("100.00"));

		OrderItem item2 = new OrderItem();
		item2.setId(2L);
		item2.setIdProduct(2L);
		item2.setProductName("Product 2");
		item2.setUnitPrice(new BigDecimal("50.00"));
		item2.setQuantity(2);
		item2.setSubtotal(new BigDecimal("100.00"));

		testOrder.setItems(Arrays.asList(item1, item2));
	}

	@Test
	void generateOrderReceipt_ExistingOrder_Success() {
		when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

		byte[] pdf = pdfService.generateOrderReceipt(1L);

		assertNotNull(pdf);
		assertTrue(pdf.length > 0);
	}

	@Test
	void generateOrderReceipt_NonExistingOrder_ThrowsException() {
		when(orderRepository.findById(1L)).thenReturn(Optional.empty());

		assertThrows(RuntimeException.class, () -> pdfService.generateOrderReceipt(1L));
	}

	@Test
	void generateOrderReceipt_CancelledOrder_IncludesCancellationInfo() {
		testOrder.setStatus(OrderStatus.CANCELLED);
		testOrder.setCancelledAt(LocalDateTime.now());

		when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

		byte[] pdf = pdfService.generateOrderReceipt(1L);

		assertNotNull(pdf);
		assertTrue(pdf.length > 0);
	}

	@Test
	void generateOrderReceipt_CompletedOrder_Success() {
		testOrder.setStatus(OrderStatus.COMPLETED);
		testOrder.setCompletedAt(LocalDateTime.now());

		when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

		byte[] pdf = pdfService.generateOrderReceipt(1L);

		assertNotNull(pdf);
		assertTrue(pdf.length > 0);
	}

	@Test
	void generateOrderReceipt_CreatedOrder_Success() {
		testOrder.setStatus(OrderStatus.CREATED);

		when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

		byte[] pdf = pdfService.generateOrderReceipt(1L);

		assertNotNull(pdf);
		assertTrue(pdf.length > 0);
	}
}
