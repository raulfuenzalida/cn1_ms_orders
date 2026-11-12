package duoc.cn1.ms_orders.security;

import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

/**
 * Soporta dos proveedores de identidad:
 *  - Microsoft Entra ID (front_admin) → ROLE_ADMIN
 *  - AWS Cognito User Pool (front_public clientes) → ROLE_CLIENT
 *
 * El decoder intenta validar primero con Entra ID y, si la firma o el
 * issuer no coinciden, reintenta con Cognito. Solo se aceptan tokens
 * emitidos por uno de los dos issuers configurados.
 */
@Configuration
public class JwtConfig {

	@Bean
	public JwtDecoder jwtDecoder(
			@Value("${app.security.jwt.entra-issuer-uri}") String entraIssuerUri,
			@Value("${app.security.jwt.entra-jwk-set-uri}") String entraJwkSetUri,
			@Value("${app.security.jwt.cognito-issuer-uri}") String cognitoIssuerUri,
			@Value("${app.security.jwt.cognito-jwk-set-uri}") String cognitoJwkSetUri) {

		NimbusJwtDecoder entraDecoder = NimbusJwtDecoder
			.withJwkSetUri(entraJwkSetUri)
			.build();
		entraDecoder.setJwtValidator(
			JwtValidators.createDefaultWithIssuer(entraIssuerUri));

		NimbusJwtDecoder cognitoDecoder = NimbusJwtDecoder
			.withJwkSetUri(cognitoJwkSetUri)
			.build();
		cognitoDecoder.setJwtValidator(
			JwtValidators.createDefaultWithIssuer(cognitoIssuerUri));

		return token -> {
			try {
				return entraDecoder.decode(token);
			} catch (JwtException entraException) {
				try {
					return cognitoDecoder.decode(token);
				} catch (JwtException cognitoException) {
					throw entraException;
				}
			}
		};
	}

	/**
	 * Asigna ROLE_ADMIN a tokens de Entra ID y ROLE_CLIENT a tokens de
	 * Cognito, según el issuer validado por el decoder.
	 */
	@Bean
	@Qualifier("jwtAuthenticationConverter")
	public Converter<Jwt, AbstractAuthenticationToken> jwtAuthenticationConverter(
			@Value("${app.security.jwt.cognito-issuer-uri}") String cognitoIssuerUri) {
		return jwt -> {
			boolean isCognito = jwt.getIssuer() != null
				&& jwt.getIssuer().toString().equals(cognitoIssuerUri);

			String authority = isCognito ? "ROLE_CLIENT" : "ROLE_ADMIN";

			return new JwtAuthenticationToken(
				jwt,
				List.of(new SimpleGrantedAuthority(authority))
			);
		};
	}
}
