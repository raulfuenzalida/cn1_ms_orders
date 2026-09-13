package duoc.cn1.ms_orders.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

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
				log.error("Producto {} no encontrado en ms_products", id);
				throw new RuntimeException("Producto no encontrado");
			}
			
			log.info("Producto {} encontrado: {} - {}", id, product.getName(), product.getFinalPrice());
			return product;
			
		} catch (Exception e) {
			log.error("Error al consultar producto {} en ms_products: {}", id, e.getMessage());
			throw new RuntimeException("Error al consultar producto en ms_products", e);
		}
	}

	public boolean isProductAvailable(ProductDto product) {
		return "ACTIVE".equals(product.getStatus()) && "CURRENT".equals(product.getPriceStatus());
	}
}
