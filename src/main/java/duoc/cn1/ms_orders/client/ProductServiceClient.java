package duoc.cn1.ms_orders.client;

import duoc.cn1.ms_orders.exception.InvalidProductResponseException;
import duoc.cn1.ms_orders.exception.ProductNotFoundException;
import duoc.cn1.ms_orders.exception.ProductServiceUnavailableException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductServiceClient {

	private final RestTemplate restTemplate;

	@Value("${ms.products.base-url:http://localhost:8081}")
	private String productsBaseUrl;

	public ProductDto getProductById(Long id) {
		String url = productsBaseUrl + "/api/v1/products/" + id;
		
		log.info("Consultando producto {} en ms_products", id);
		
		try {
			ProductDto product = restTemplate.getForObject(url, ProductDto.class);
			
			if (product == null) {
				log.error("Producto {} no encontrado en ms_products - respuesta nula", id);
				throw new InvalidProductResponseException("Respuesta nula del servicio de productos para ID: " + id);
			}
			
			log.info("Producto {} encontrado: {} - {}", id, product.getName(), product.getFinalPrice());
			return product;
			
		} catch (ProductNotFoundException | InvalidProductResponseException | ProductServiceUnavailableException e) {
			throw e;
		} catch (RestClientException e) {
			log.error("Error de conexión al consultar producto {} en ms_products: {}", id, e.getMessage());
			throw new ProductServiceUnavailableException("Error de conexión con ms_products: " + e.getMessage());
		} catch (Exception e) {
			log.error("Error inesperado al consultar producto {} en ms_products: {}", id, e.getMessage());
			throw new ProductServiceUnavailableException("Error inesperado en ms_products: " + e.getMessage());
		}
	}

	public boolean isProductAvailable(ProductDto product) {
		return "ACTIVE".equals(product.getStatus()) && "CURRENT".equals(product.getPriceStatus());
	}
}
