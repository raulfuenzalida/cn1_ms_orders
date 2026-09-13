package duoc.cn1.ms_orders;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
	"spring.security.oauth2.resourceserver.jwt.issuer-uri=http://dummy-issuer",
	"spring.sql.init.mode=never",
	"spring.datasource.url=jdbc:h2:mem:testdb",
	"spring.datasource.driver-class-name=org.h2.Driver",
	"spring.jpa.hibernate.ddl-auto=create-drop"
})
class MsOrdersApplicationTests {

	@Test
	void contextLoads() {
	}
}
