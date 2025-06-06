package com.epam.aidial.cfg.configuration;

import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.implementation.AccessTokenCache;
import com.epam.aidial.cfg.configuration.datasource.DynamicPasswordHikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Supplier;
import javax.sql.DataSource;

@Configuration
@Slf4j
@ConditionalOnProperty(name = "datasource.vendor", havingValue = "POSTGRES")
public class PostgresConfiguration {

    private static final String AAD_DATABASE_SCOPE = "https://ossrdbms-aad.database.windows.net/.default";
    private static final TokenRequestContext TOKEN_REQUEST_CONTEXT = new TokenRequestContext().addScopes(AAD_DATABASE_SCOPE);

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
    public DataSource managedAzureAuthTypeDataSource(@Value("${postgres.datasource.url}") String url,
                                                     @Value("${postgres.datasource.driver-class-name}") String driverClassName,
                                                     @Value("${postgres.datasource.username}") String username,
                                                     TokenCredential tokenCredential) {
        AccessTokenCache accessTokenCache = new AccessTokenCache(tokenCredential);
        Supplier<String> passwordProvider = () -> accessTokenCache.getTokenSync(TOKEN_REQUEST_CONTEXT, true).getToken();

        DynamicPasswordHikariDataSource dynamicPasswordHikariDataSource = new DynamicPasswordHikariDataSource(passwordProvider);
        dynamicPasswordHikariDataSource.setDriverClassName(driverClassName);
        dynamicPasswordHikariDataSource.setJdbcUrl(url);
        dynamicPasswordHikariDataSource.setUsername(username);

        return dynamicPasswordHikariDataSource;
    }

}
