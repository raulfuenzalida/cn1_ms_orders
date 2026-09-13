package duoc.cn1.ms_orders.exception;

public class ProductNotAvailableException extends RuntimeException {

	public ProductNotAvailableException(Long id) {
		super("El producto con ID " + id + " no está disponible");
	}

	public ProductNotAvailableException(String message) {
		super(message);
	}
}
