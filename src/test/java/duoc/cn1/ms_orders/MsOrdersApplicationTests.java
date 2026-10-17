package duoc.cn1.ms_orders;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
	"spring.security.oauth2.resourceserver.jwt.issuer-uri=http://dummy-issuer",
	"spring.sql.init.mode=never",
	"spring.datasource.url=jdbc:h2:mem:testdb",
	"spring.datasource.driver-class-name=org.h2.Driver",
	"spring.jpa.hibernate.ddl-auto=create-drop",
	"spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration"
})
class MsOrdersApplicationTests {

	@Disabled("Context test - temporarily disabled due to H2 schema issues")
	@Test
	void contextLoads() {
	}
}
