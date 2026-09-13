package duoc.cn1.ms_orders.exception;

public class OrderNotFoundException extends RuntimeException {

	public OrderNotFoundException(Long id) {
		super("No se encontró el pedido solicitado con ID: " + id);
	}

	public OrderNotFoundException(String message) {
		super(message);
	}
}
