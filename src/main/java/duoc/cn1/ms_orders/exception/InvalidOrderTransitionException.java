package duoc.cn1.ms_orders.exception;

public class InvalidOrderTransitionException extends RuntimeException {

	public InvalidOrderTransitionException(String fromStatus, String toStatus) {
		super("No se puede cambiar el estado del pedido de " + fromStatus + " a " + toStatus);
	}

	public InvalidOrderTransitionException(String message) {
		super(message);
	}
}
