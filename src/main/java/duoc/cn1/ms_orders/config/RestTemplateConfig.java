package duoc.cn1.ms_orders.config;

import duoc.cn1.ms_orders.exception.ProductNotFoundException;
import duoc.cn1.ms_orders.exception.ProductServiceUnavailableException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

@Configuration
public class RestTemplateConfig {

	@Bean
	public RestTemplate restTemplate() {
		RestTemplate restTemplate = new RestTemplate();
		restTemplate.setErrorHandler(new ProductServiceErrorHandler());
		return restTemplate;
	}

	private static class ProductServiceErrorHandler extends DefaultResponseErrorHandler {
		@Override
		public void handleError(ClientHttpResponse response) throws IOException {
			HttpStatus statusCode = (HttpStatus) response.getStatusCode();
			
			if (statusCode == HttpStatus.NOT_FOUND) {
				throw new ProductNotFoundException("Producto no encontrado en ms_products");
			}
			
			if (statusCode.is5xxServerError()) {
				throw new ProductServiceUnavailableException("ms_products retornó error: " + statusCode);
			}
			
			super.handleError(response);
		}
	}
}
