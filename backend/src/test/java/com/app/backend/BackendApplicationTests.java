package com.app.backend;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Integration smoke test verifying the application context loads against a live database.
 * Requires Docker Compose services to be running. Skipped in CI unless
 * the INTEGRATION_TESTS_ENABLED environment variable is set to "true".
 */
@SpringBootTest
@ActiveProfiles("local")
@EnabledIfEnvironmentVariable(named = "INTEGRATION_TESTS_ENABLED", matches = "true")
class BackendApplicationTests {

    @Test
    void contextLoads() {
    }
}
