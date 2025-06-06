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
@ConditionalOnProperty(name = "datasource.vendor", havingValue = "MS_SQL_SERVER")
public class MsSqlServerConfiguration {

    @Bean
    @ConditionalOnProperty(value = "datasource.auth.type", havingValue = "basic")
    public DataSource dataSource(@Value("${sqlserver.datasource.url}") String url,
                                 @Value("${sqlserver.datasource.driver-class-name}") String driverClassName,
                                 @Value("${sqlserver.datasource.username}") String username,
                                 @Value("${sqlserver.datasource.password}") String dbPassword) {
        return DataSourceBuilder.create()
                .driverClassName(driverClassName)
                .url(url)
                .username(username)
                .password(dbPassword)
                .build();
    }

    @Bean
    @ConditionalOnProperty(value = "datasource.auth.type", havingValue = "azure")
    public DataSource managedAzureAuthTypeDataSource(@Value("${sqlserver.datasource.url}") String url,
                                                     @Value("${sqlserver.datasource.driver-class-name}") String driverClassName,
                                                     @Value("${azure.auth.clientId}") String azureClientId) {
        HikariConfig hikariConfig = new HikariConfig();

        hikariConfig.setDriverClassName(driverClassName);
        hikariConfig.setJdbcUrl(url);
        hikariConfig.addDataSourceProperty("msiClientId", azureClientId);
        hikariConfig.addDataSourceProperty("authentication", "ActiveDirectoryMSI");

        return new HikariDataSource(hikariConfig);
    }

}
