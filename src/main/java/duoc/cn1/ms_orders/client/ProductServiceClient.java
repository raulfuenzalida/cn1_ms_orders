package duoc.cn1.ms_orders.client;

import duoc.cn1.ms_orders.exception.InvalidProductResponseException;
import duoc.cn1.ms_orders.exception.ProductNotFoundException;
import duoc.cn1.ms_orders.exception.ProductServiceUnavailableException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
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

        log.info(
            "Consultando producto {} en ms_products",
            id
        );

        try {
            ProductDto product = restTemplate.getForObject(
                url,
                ProductDto.class
            );

            validateProductResponse(product, id);

            log.info(
                "Producto {} encontrado: {} - {}",
                id,
                product.getName(),
                product.getFinalPrice()
            );

            return product;

        } catch (
            ProductNotFoundException |
            InvalidProductResponseException |
            ProductServiceUnavailableException e
        ) {
            throw e;

        } catch (RestClientException e) {
            log.error(
                "Error de conexión al consultar producto {} en ms_products: {}",
                id,
                e.getMessage()
            );

            throw new ProductServiceUnavailableException(
                "Error de conexión con ms_products: "
                    + e.getMessage()
            );

        } catch (Exception e) {
            log.error(
                "Error inesperado al consultar producto {} en ms_products: {}",
                id,
                e.getMessage()
            );

            throw new ProductServiceUnavailableException(
                "Error inesperado en ms_products: "
                    + e.getMessage()
            );
        }
    }

    private void validateProductResponse(
            ProductDto product,
            Long requestedId) {

        if (product == null) {
            throw new InvalidProductResponseException(
                "Respuesta nula del servicio de productos para ID: "
                    + requestedId
            );
        }

        if (
            product.getId() == null ||
            product.getName() == null ||
            product.getName().isBlank() ||
            product.getFinalPrice() == null
        ) {
            throw new InvalidProductResponseException(
                "Respuesta del servicio de productos con datos incompletos para ID: "
                    + requestedId
            );
        }

        if (!requestedId.equals(product.getId())) {
            throw new InvalidProductResponseException(
                "El ID retornado por ms_products no coincide con el producto solicitado"
            );
        }
    }
}