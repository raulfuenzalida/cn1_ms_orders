package duoc.cn1.ms_orders.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(OrderNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleOrderNotFoundException(
			OrderNotFoundException ex, WebRequest request) {
		ErrorResponse error = new ErrorResponse(
			LocalDateTime.now(),
			HttpStatus.NOT_FOUND.value(),
			HttpStatus.NOT_FOUND.getReasonPhrase(),
			"ORDER_NOT_FOUND",
			ex.getMessage(),
			request.getDescription(false).replace("uri=", "")
		);
		return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
	}

	@ExceptionHandler(InvalidOrderTransitionException.class)
	public ResponseEntity<ErrorResponse> handleInvalidOrderTransitionException(
			InvalidOrderTransitionException ex, WebRequest request) {
		ErrorResponse error = new ErrorResponse(
			LocalDateTime.now(),
			HttpStatus.BAD_REQUEST.value(),
			HttpStatus.BAD_REQUEST.getReasonPhrase(),
			"INVALID_ORDER_TRANSITION",
			ex.getMessage(),
			request.getDescription(false).replace("uri=", "")
		);
		return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(InvalidQuantityException.class)
	public ResponseEntity<ErrorResponse> handleInvalidQuantityException(
			InvalidQuantityException ex, WebRequest request) {
		ErrorResponse error = new ErrorResponse(
			LocalDateTime.now(),
			HttpStatus.BAD_REQUEST.value(),
			HttpStatus.BAD_REQUEST.getReasonPhrase(),
			"INVALID_QUANTITY",
			ex.getMessage(),
			request.getDescription(false).replace("uri=", "")
		);
		return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(TooManyDistinctProductsException.class)
	public ResponseEntity<ErrorResponse> handleTooManyDistinctProductsException(
			TooManyDistinctProductsException ex, WebRequest request) {
		ErrorResponse error = new ErrorResponse(
			LocalDateTime.now(),
			HttpStatus.BAD_REQUEST.value(),
			HttpStatus.BAD_REQUEST.getReasonPhrase(),
			"TOO_MANY_DISTINCT_PRODUCTS",
			ex.getMessage(),
			request.getDescription(false).replace("uri=", "")
		);
		return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(DuplicateProductException.class)
	public ResponseEntity<ErrorResponse> handleDuplicateProductException(
			DuplicateProductException ex, WebRequest request) {
		ErrorResponse error = new ErrorResponse(
			LocalDateTime.now(),
			HttpStatus.CONFLICT.value(),
			HttpStatus.CONFLICT.getReasonPhrase(),
			"DUPLICATE_PRODUCT",
			ex.getMessage(),
			request.getDescription(false).replace("uri=", "")
		);
		return new ResponseEntity<>(error, HttpStatus.CONFLICT);
	}

	@ExceptionHandler(ProductNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleProductNotFoundException(
			ProductNotFoundException ex, WebRequest request) {
		ErrorResponse error = new ErrorResponse(
			LocalDateTime.now(),
			HttpStatus.NOT_FOUND.value(),
			HttpStatus.NOT_FOUND.getReasonPhrase(),
			"PRODUCT_NOT_FOUND",
			ex.getMessage(),
			request.getDescription(false).replace("uri=", "")
		);
		return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
	}

	@ExceptionHandler(ProductNotAvailableException.class)
	public ResponseEntity<ErrorResponse> handleProductNotAvailableException(
			ProductNotAvailableException ex, WebRequest request) {
		ErrorResponse error = new ErrorResponse(
			LocalDateTime.now(),
			HttpStatus.BAD_REQUEST.value(),
			HttpStatus.BAD_REQUEST.getReasonPhrase(),
			"PRODUCT_NOT_AVAILABLE",
			ex.getMessage(),
			request.getDescription(false).replace("uri=", "")
		);
		return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(ProductServiceUnavailableException.class)
	public ResponseEntity<ErrorResponse> handleProductServiceUnavailableException(
			ProductServiceUnavailableException ex, WebRequest request) {
		ErrorResponse error = new ErrorResponse(
			LocalDateTime.now(),
			HttpStatus.SERVICE_UNAVAILABLE.value(),
			HttpStatus.SERVICE_UNAVAILABLE.getReasonPhrase(),
			"PRODUCT_SERVICE_UNAVAILABLE",
			ex.getMessage(),
			request.getDescription(false).replace("uri=", "")
		);
		return new ResponseEntity<>(error, HttpStatus.SERVICE_UNAVAILABLE);
	}

	@ExceptionHandler(InvalidProductResponseException.class)
	public ResponseEntity<ErrorResponse> handleInvalidProductResponseException(
			InvalidProductResponseException ex, WebRequest request) {
		ErrorResponse error = new ErrorResponse(
			LocalDateTime.now(),
			HttpStatus.BAD_REQUEST.value(),
			HttpStatus.BAD_REQUEST.getReasonPhrase(),
			"INVALID_PRODUCT_RESPONSE",
			ex.getMessage(),
			request.getDescription(false).replace("uri=", "")
		);
		return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(InvalidCustomerDataException.class)
	public ResponseEntity<ErrorResponse> handleInvalidCustomerDataException(
			InvalidCustomerDataException ex, WebRequest request) {
		ErrorResponse error = new ErrorResponse(
			LocalDateTime.now(),
			HttpStatus.BAD_REQUEST.value(),
			HttpStatus.BAD_REQUEST.getReasonPhrase(),
			"INVALID_CUSTOMER_DATA",
			ex.getMessage(),
			request.getDescription(false).replace("uri=", "")
		);
		return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleValidationException(
			MethodArgumentNotValidException ex, WebRequest request) {
		Map<String, String> errors = new HashMap<>();
		ex.getBindingResult().getAllErrors().forEach(error -> {
			String fieldName = ((FieldError) error).getField();
			String errorMessage = error.getDefaultMessage();
			errors.put(fieldName, errorMessage);
		});

		ErrorResponse error = new ErrorResponse(
			LocalDateTime.now(),
			HttpStatus.BAD_REQUEST.value(),
			HttpStatus.BAD_REQUEST.getReasonPhrase(),
			"VALIDATION_ERROR",
			"Error de validación en los campos enviados",
			request.getDescription(false).replace("uri=", ""),
			errors
		);
		return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
			IllegalArgumentException ex, WebRequest request) {
		ErrorResponse error = new ErrorResponse(
			LocalDateTime.now(),
			HttpStatus.BAD_REQUEST.value(),
			HttpStatus.BAD_REQUEST.getReasonPhrase(),
			"INVALID_OPERATION",
			ex.getMessage(),
			request.getDescription(false).replace("uri=", "")
		);
		return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleGlobalException(
			Exception ex, WebRequest request) {
		ErrorResponse error = new ErrorResponse(
			LocalDateTime.now(),
			HttpStatus.INTERNAL_SERVER_ERROR.value(),
			HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
			"INTERNAL_ERROR",
			"Error interno del servidor",
			request.getDescription(false).replace("uri=", "")
		);
		return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
	}
}
