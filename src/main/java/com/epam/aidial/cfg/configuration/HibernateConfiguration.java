package com.epam.aidial.cfg.configuration;

import com.epam.aidial.cfg.dao.hibernate.integrator.CustomIntegratorProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Set;

@Configuration(proxyBeanMethods = false)
public class HibernateConfiguration {

    private static final Set<String> DATABASES_REQUIRE_QUOTE_KEYWORDS = Set.of("MS_SQL_SERVER");

    @Bean
    public HibernatePropertiesCustomizer hibernatePropertiesCustomizer(@Value("${datasource.vendor}") String datasourceVendor) {
        return hibernateProperties -> {
            if (DATABASES_REQUIRE_QUOTE_KEYWORDS.contains(datasourceVendor)) {
                hibernateProperties.put("hibernate.auto_quote_keyword", true);
            }
            hibernateProperties.put("hibernate.integrator_provider", new CustomIntegratorProvider());
        };
    }
}
