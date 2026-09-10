package com.epam.aidial.cfg.functional.config;

import com.epam.aidial.cfg.functional.config.persistence.PostgresTestPersistenceService;
import com.epam.aidial.cfg.functional.config.persistence.TestPersistenceService;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

@TestConfiguration
@Import(FunctionalTestConfiguration.class)
public class PostgresFunctionalTestConfiguration {

    @Bean
    public TestPersistenceService testPersistenceService(HikariDataSource hikariDataSource) {
        return new PostgresTestPersistenceService(hikariDataSource);
    }
}
