package duoc.cn1.ms_orders.exception;

public class DuplicateProductException extends RuntimeException {

	public DuplicateProductException(Long idProduct) {
		super("El producto con ID " + idProduct + " ya está incluido en el pedido");
	}

	public DuplicateProductException(String message) {
		super(message);
	}
}
