package com.epam.aidial.cfg.configuration;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
@Slf4j
@ConditionalOnProperty(name = "datasource.vendor", havingValue = "POSTGRES")
public class PostgresConfiguration {

    @Bean
    @ConditionalOnProperty(value = "datasource.auth.type", havingValue = "basic")
    public DataSource dataSource(@Value("${postgres.datasource.url}") String url,
                                 @Value("${postgres.datasource.driver-class-name}") String driverClassName,
                                 @Value("${postgres.datasource.username}") String username,
                                 @Value("${postgres.datasource.password}") String dbPassword) {
        return DataSourceBuilder.create()
                .driverClassName(driverClassName)
                .url(url)
                .username(username)
                .password(dbPassword)
                .build();
    }

    @Bean
    @ConditionalOnProperty(value = "datasource.auth.type", havingValue = "azure")
    public DataSource azureAuthTypeDataSource(@Value("${postgres.datasource.url}") String url,
                                              @Value("${postgres.datasource.driver-class-name}") String driverClassName,
                                              @Value("${postgres.datasource.username}") String username) {
        HikariConfig hikariConfig = new HikariConfig();

        hikariConfig.setUsername(username);
        hikariConfig.setDriverClassName(driverClassName);
        hikariConfig.setJdbcUrl(url);
        hikariConfig.addDataSourceProperty("authenticationPluginClassName", "com.azure.identity.extensions.jdbc.postgresql.AzurePostgresqlAuthenticationPlugin");

        return new HikariDataSource(hikariConfig);
    }

    @Bean
    @ConditionalOnProperty(value = "datasource.auth.type", havingValue = "gcp")
    public DataSource gcpAuthTypeDataSource(@Value("${postgres.datasource.url}") String url,
                                            @Value("${postgres.datasource.driver-class-name}") String driverClassName,
                                            @Value("${postgres.datasource.username}") String username) {
        HikariConfig hikariConfig = new HikariConfig();

        hikariConfig.setUsername(username);
        hikariConfig.setDriverClassName(driverClassName);
        hikariConfig.setJdbcUrl(url);
        hikariConfig.addDataSourceProperty("socketFactory", "com.google.cloud.sql.postgres.SocketFactory");
        hikariConfig.addDataSourceProperty("enableIamAuth", "true");

        return new HikariDataSource(hikariConfig);
    }

}
