package duoc.cn1.ms_orders.exception;

public class ProductServiceUnavailableException extends RuntimeException {

	public ProductServiceUnavailableException() {
		super("El servicio de productos no está disponible");
	}

	public ProductServiceUnavailableException(String message) {
		super(message);
	}
}
