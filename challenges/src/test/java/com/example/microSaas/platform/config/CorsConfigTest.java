package com.example.microSaas.platform.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;

class CorsConfigTest {

    @Test
    void allowsAnyOriginWithoutCredentialsForApiRoutes() {
        TestCorsRegistry registry = new TestCorsRegistry();
        new CorsConfig("*").addCorsMappings(registry);

        CorsConfiguration configuration =
            registry.configurations().get("/api/**");

        assertNotNull(configuration);
        assertEquals("*", configuration.checkOrigin("https://example.com"));
        assertFalse(configuration.getAllowCredentials());
        assertEquals(
            java.util.List.of("GET", "POST", "DELETE", "OPTIONS"),
            configuration.getAllowedMethods()
        );
    }

    private static class TestCorsRegistry extends CorsRegistry {
        Map<String, CorsConfiguration> configurations() {
            return getCorsConfigurations();
        }
    }
}
