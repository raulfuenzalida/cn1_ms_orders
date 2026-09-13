package duoc.cn1.ms_orders.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

	private LocalDateTime timestamp;
	private int status;
	private String error;
	private String code;
	private String message;
	private String path;
	private Map<String, String> details;

	public ErrorResponse() {
	}

	public ErrorResponse(LocalDateTime timestamp, int status, String error, String code, String message, String path) {
		this.timestamp = timestamp;
		this.status = status;
		this.error = error;
		this.code = code;
		this.message = message;
		this.path = path;
	}

	public ErrorResponse(LocalDateTime timestamp, int status, String error, String code, String message, String path, Map<String, String> details) {
		this.timestamp = timestamp;
		this.status = status;
		this.error = error;
		this.code = code;
		this.message = message;
		this.path = path;
		this.details = details;
	}
}
