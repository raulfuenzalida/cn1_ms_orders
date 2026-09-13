package duoc.cn1.ms_orders.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

	@Bean
	public OpenAPI msOrdersOpenAPI() {
		final String securitySchemeName = "oauth2";

		return new OpenAPI()
				.info(new Info()
						.title("MS-Orders API")
						.description("API REST para gestión de pedidos de PrintWorks")
						.version("1.0.0")
						.contact(new Contact()
								.name("Duoc UC")
								.email("contacto@duoc.cl"))
						.license(new License()
								.name("Apache 2.0")
								.url("https://www.apache.org/licenses/LICENSE-2.0.html")))
				.servers(List.of(
						new Server().url("http://localhost:8082").description("Servidor de desarrollo"),
						new Server().url("https://1335t86sik.execute-api.us-east-1.amazonaws.com").description("Servidor de producción")
				))
				.addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
				.components(new io.swagger.v3.oas.models.Components()
						.addSecuritySchemes(securitySchemeName, new SecurityScheme()
								.name(securitySchemeName)
								.type(SecurityScheme.Type.HTTP)
								.scheme("bearer")
								.bearerFormat("JWT")
								.description("Autenticación mediante OAuth2 Access Token de Microsoft Entra ID")));
	}
}
