package com.spin.transaction.integration;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.spin.transaction.TransactionApiApplication;
import com.spin.transaction.infrastructure.persistence.repository.SpringDataTransactionRepository;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import org.springframework.test.annotation.DirtiesContext;
/**
 * Base class for integration tests.
 *
 * Starts:
 * - The complete Spring Boot application context.
 * - A real PostgreSQL database through Testcontainers.
 * - An embedded WireMock server that simulates the external provider.
 */
@SpringBootTest(classes = TransactionApiApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public abstract class AbstractIntegrationTest {

    /**
     * Real PostgreSQL instance created exclusively for integration tests.
     *
     * The container is shared by every test method in the test class.
     */
    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:17-alpine")
                    .withDatabaseName("transactions_test_db")
                    .withUsername("test")
                    .withPassword("test");

    /**
     * Embedded HTTP server that simulates the external transaction provider.
     *
     * A random available port is used to avoid port conflicts.
     */
    protected static final WireMockServer WIRE_MOCK_SERVER =
            new WireMockServer(
                    wireMockConfig()
                            .dynamicPort()
                            .http2PlainDisabled(true)
                            .gzipDisabled(true)
            );

    static {
        WIRE_MOCK_SERVER.start();
    }

    /**
     * MockMvc executes HTTP requests against the Spring MVC application
     * without requiring a real application server port.
     */
    @Autowired
    protected MockMvc mockMvc;

    /**
     * Real Spring Data repository connected to the PostgreSQL container.
     *
     * It is used to prepare and verify database state.
     */
    @Autowired
    protected SpringDataTransactionRepository repository;

    /**
     * Replaces the normal external-provider URL with the URL assigned
     * dynamically to WireMock.
     */
    @DynamicPropertySource
    static void registerDynamicProperties(
            DynamicPropertyRegistry registry) {

        registry.add(
                "transaction.provider.base-url",
                WIRE_MOCK_SERVER::baseUrl
        );
    }

    /**
     * Ensures that every integration test begins with:
     * - An empty database.
     * - No WireMock stubs or recorded HTTP requests.
     */
    @BeforeEach
    void cleanIntegrationEnvironment() {

        repository.deleteAll();
        WIRE_MOCK_SERVER.resetAll();
    }

    /**
     * Stops WireMock after all integration tests have finished.
     */
    @AfterAll
    static void stopWireMock() {

        if (WIRE_MOCK_SERVER.isRunning()) {
            WIRE_MOCK_SERVER.stop();
        }
    }
}