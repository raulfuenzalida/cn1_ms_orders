package duoc.cn1.ms_orders.security;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class SecurityConfig {

	@Value("${cors.allowed-origins:http://localhost:5173,https://raulfuenzalida.github.io}")
	private String corsAllowedOrigins;

	@Value("${app.security.jwt.cognito-issuer-uri}")
	private String cognitoIssuerUri;

	@Bean
	SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http
				.csrf(AbstractHttpConfigurer::disable)
				.cors(Customizer.withDefaults())
				.authorizeHttpRequests(auth -> auth
						.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
						.requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/api-docs/**").permitAll()
						.requestMatchers(HttpMethod.POST, "/api/v1/orders/crear")
							.hasAnyRole("CLIENT", "ADMIN")
						.requestMatchers(HttpMethod.GET, "/api/v1/orders/*/receipt")
							.hasAnyRole("CLIENT", "ADMIN")
						.requestMatchers("/api/v1/orders/**").hasRole("ADMIN")
						.anyRequest().denyAll())
				.oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt ->
						jwt.jwtAuthenticationConverter(this::jwtAuthenticationConverter)));
		return http.build();
	}

	/**
	 * Asigna ROLE_ADMIN a tokens de Entra ID y ROLE_CLIENT a tokens de
	 * Cognito, según el issuer validado por el JwtDecoder.
	 * No es un bean: Spring MVC intentaría usarlo como Converter web.
	 */
	private AbstractAuthenticationToken jwtAuthenticationConverter(Jwt jwt) {
		boolean isCognito = jwt.getIssuer() != null
			&& jwt.getIssuer().toString().equals(cognitoIssuerUri);

		String authority = isCognito ? "ROLE_CLIENT" : "ROLE_ADMIN";

		return new JwtAuthenticationToken(
			jwt,
			List.of(new SimpleGrantedAuthority(authority))
		);
	}

	@Bean
	CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration cfg = new CorsConfiguration();
		cfg.setAllowedOrigins(List.of(corsAllowedOrigins.split(",")));
		cfg.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
		cfg.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept", "Origin"));
		cfg.setAllowCredentials(true);
		cfg.setMaxAge(3600L);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", cfg);
		return source;
	}
}
