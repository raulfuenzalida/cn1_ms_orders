package duoc.cn1.ms_orders.dto;

import duoc.cn1.ms_orders.dto.request.OrderCreateRequest;
import duoc.cn1.ms_orders.dto.request.OrderItemRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class OrderDtoValidationTest {

	private Validator validator;

	@BeforeEach
	void setUp() {
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		validator = factory.getValidator();
	}

	@Test
	void orderCreateRequest_ValidData_NoViolations() {
		OrderCreateRequest request = new OrderCreateRequest();
		request.setCustomerName("Test Customer");
		request.setCustomerEmail("test@example.com");
		
		OrderItemRequest item = new OrderItemRequest();
		item.setIdProduct(1L);
		item.setQuantity(2);
		
		request.setItems(Collections.singletonList(item));

		Set<ConstraintViolation<OrderCreateRequest>> violations = validator.validate(request);
		
		assertTrue(violations.isEmpty());
	}

	@Test
	void orderCreateRequest_EmptyCustomerName_Violation() {
		OrderCreateRequest request = new OrderCreateRequest();
		request.setCustomerName("");
		request.setCustomerEmail("test@example.com");
		
		OrderItemRequest item = new OrderItemRequest();
		item.setIdProduct(1L);
		item.setQuantity(2);
		
		request.setItems(Collections.singletonList(item));

		Set<ConstraintViolation<OrderCreateRequest>> violations = validator.validate(request);
		
		assertFalse(violations.isEmpty());
		assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("customerName")));
	}

	@Test
	void orderCreateRequest_InvalidEmail_Violation() {
		OrderCreateRequest request = new OrderCreateRequest();
		request.setCustomerName("Test Customer");
		request.setCustomerEmail("invalid-email");
		
		OrderItemRequest item = new OrderItemRequest();
		item.setIdProduct(1L);
		item.setQuantity(2);
		
		request.setItems(Collections.singletonList(item));

		Set<ConstraintViolation<OrderCreateRequest>> violations = validator.validate(request);
		
		assertFalse(violations.isEmpty());
		assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("customerEmail")));
	}

	@Test
	void orderCreateRequest_EmptyItems_Violation() {
		OrderCreateRequest request = new OrderCreateRequest();
		request.setCustomerName("Test Customer");
		request.setCustomerEmail("test@example.com");
		request.setItems(Collections.emptyList());

		Set<ConstraintViolation<OrderCreateRequest>> violations = validator.validate(request);
		
		assertFalse(violations.isEmpty());
		assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("items")));
	}

	@Test
	void orderItemRequest_ValidData_NoViolations() {
		OrderItemRequest request = new OrderItemRequest();
		request.setIdProduct(1L);
		request.setQuantity(5);

		Set<ConstraintViolation<OrderItemRequest>> violations = validator.validate(request);
		
		assertTrue(violations.isEmpty());
	}

	@Test
	void orderItemRequest_NullIdProduct_Violation() {
		OrderItemRequest request = new OrderItemRequest();
		request.setIdProduct(null);
		request.setQuantity(5);

		Set<ConstraintViolation<OrderItemRequest>> violations = validator.validate(request);
		
		assertFalse(violations.isEmpty());
		assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("idProduct")));
	}

	@Test
	void orderItemRequest_QuantityZero_Violation() {
		OrderItemRequest request = new OrderItemRequest();
		request.setIdProduct(1L);
		request.setQuantity(0);

		Set<ConstraintViolation<OrderItemRequest>> violations = validator.validate(request);
		
		assertFalse(violations.isEmpty());
		assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("quantity")));
	}

	@Test
	void orderItemRequest_QuantityNegative_Violation() {
		OrderItemRequest request = new OrderItemRequest();
		request.setIdProduct(1L);
		request.setQuantity(-1);

		Set<ConstraintViolation<OrderItemRequest>> violations = validator.validate(request);
		
		assertFalse(violations.isEmpty());
		assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("quantity")));
	}

	@Test
	void orderCreateRequest_TooManyItems_Violation() {
		OrderCreateRequest request = new OrderCreateRequest();
		request.setCustomerName("Test Customer");
		request.setCustomerEmail("test@example.com");
		
		request.setItems(Collections.nCopies(31, new OrderItemRequest()));

		Set<ConstraintViolation<OrderCreateRequest>> violations = validator.validate(request);
		
		assertFalse(violations.isEmpty());
		assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("items")));
	}
}
