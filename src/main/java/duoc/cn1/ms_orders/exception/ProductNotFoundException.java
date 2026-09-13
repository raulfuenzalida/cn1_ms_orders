package duoc.cn1.ms_orders.exception;

public class ProductNotFoundException extends RuntimeException {

	public ProductNotFoundException(Long id) {
		super("No se encontró el producto con ID: " + id);
	}

	public ProductNotFoundException(String message) {
		super(message);
	}
}
