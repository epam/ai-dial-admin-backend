package com.epam.aidial.cfg.configuration;

import com.epam.aidial.cfg.dao.hibernate.integrator.CustomIntegratorProvider;
import com.epam.aidial.cfg.transaction.timestamp.TransactionTimestampContext;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.type.SqlTypes;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.hibernate.autoconfigure.HibernatePropertiesCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Set;

import static org.hibernate.cfg.MappingSettings.KEYWORD_AUTO_QUOTING_ENABLED;

@Configuration(proxyBeanMethods = false)
public class HibernateConfiguration {

    private static final Set<String> DATABASES_REQUIRE_QUOTE_KEYWORDS = Set.of("MS_SQL_SERVER");
    // Hibernate 7 stores basic collections as xml on these databases, while the existing schema keeps them as serialized varbinary
    private static final Set<String> DATABASES_REQUIRE_VARBINARY_ARRAY_JDBC_TYPE = Set.of("MS_SQL_SERVER");

    @Bean
    public HibernatePropertiesCustomizer hibernatePropertiesCustomizer(
            @Value("${datasource.vendor}") String datasourceVendor,
            TransactionTimestampContext transactionTimestampContext) {
        return hibernateProperties -> {
            if (DATABASES_REQUIRE_QUOTE_KEYWORDS.contains(datasourceVendor)) {
                hibernateProperties.put(KEYWORD_AUTO_QUOTING_ENABLED, true);
            }
            if (DATABASES_REQUIRE_VARBINARY_ARRAY_JDBC_TYPE.contains(datasourceVendor)) {
                hibernateProperties.put(AvailableSettings.PREFERRED_ARRAY_JDBC_TYPE, SqlTypes.VARBINARY);
            }
            hibernateProperties.put("hibernate.integrator_provider", new CustomIntegratorProvider(transactionTimestampContext));
        };
    }
}
