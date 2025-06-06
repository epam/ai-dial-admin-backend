package com.epam.aidial.cfg.configuration;

import com.epam.aidial.cfg.transaction.timestamp.TransactionTimestampContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.time.Instant;
import java.util.Optional;

@Configuration
@EnableJpaRepositories(basePackages = {"com.epam.aidial.cfg.dao.jpa", "com.epam.aidial.cfg.dao.audit.jpa"})
@EnableJpaAuditing(dateTimeProviderRef = "transactionDateTimeProvider")
public class JpaConfiguration {

    @Bean
    public DateTimeProvider transactionDateTimeProvider(TransactionTimestampContext transactionTimestampContext) {
        return () -> Optional.of(Instant.ofEpochMilli(transactionTimestampContext.getTimestamp()));
    }
}

