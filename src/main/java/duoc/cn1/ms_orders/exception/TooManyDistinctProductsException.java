package duoc.cn1.ms_orders.exception;

public class TooManyDistinctProductsException extends RuntimeException {

	public TooManyDistinctProductsException(int count) {
		super("El pedido no puede tener más de 30 productos distintos. Productos proporcionados: " + count);
	}

	public TooManyDistinctProductsException(String message) {
		super(message);
	}
}
