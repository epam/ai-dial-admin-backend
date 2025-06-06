package com.epam.aidial.cfg.functional.config;

import com.epam.aidial.cfg.functional.config.persistence.H2TestPersistenceService;
import com.epam.aidial.cfg.functional.config.persistence.TestPersistenceService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

@TestConfiguration
@Import(FunctionalTestConfiguration.class)
public class H2FunctionalTestConfiguration {

    @Bean
    public TestPersistenceService testPersistenceService() {
        return new H2TestPersistenceService();
    }
}
