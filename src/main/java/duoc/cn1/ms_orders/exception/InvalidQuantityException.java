package duoc.cn1.ms_orders.exception;

public class InvalidQuantityException extends RuntimeException {

	public InvalidQuantityException(Integer quantity) {
		super("La cantidad debe estar entre 1 y 99. Valor proporcionado: " + quantity);
	}

	public InvalidQuantityException(String message) {
		super(message);
	}
}
