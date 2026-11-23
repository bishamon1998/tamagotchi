package com.tamagotchi;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.postgresql.PostgreSQLContainer;

/** A throwaway PostgreSQL in Docker for the integration tests; one container serves all test contexts. */
@TestConfiguration(proxyBeanMethods = false)
class TestDatabase {

    private static final String IMAGE = "postgres:18-alpine";
    private static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer(IMAGE);

    @Bean
    @ServiceConnection
    PostgreSQLContainer postgres() {
        return POSTGRES;
    }
}
