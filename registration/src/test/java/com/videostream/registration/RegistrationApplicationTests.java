package com.videostream.registration;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
		"spring.security.oauth2.resourceserver.jwt.jwk-set-uri=http://localhost:9999/mock",
		"spring.security.oauth2.resourceserver.jwt.issuer-uri=http://localhost:9999/mock",
		"keycloak.url=http://localhost:9999/mock",
		"keycloak.realm=test-realm",
		"keycloak.client-id=test-client",
		"keycloak.client-secret=test-secret"
})
class RegistrationApplicationTests {

	@Test
	void contextLoads() {
	}
}
