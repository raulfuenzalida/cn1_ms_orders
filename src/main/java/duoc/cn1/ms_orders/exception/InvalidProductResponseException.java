package duoc.cn1.ms_orders.exception;

public class InvalidProductResponseException extends RuntimeException {

	public InvalidProductResponseException() {
		super("Respuesta inválida del servicio de productos");
	}

	public InvalidProductResponseException(String message) {
		super(message);
	}
}
