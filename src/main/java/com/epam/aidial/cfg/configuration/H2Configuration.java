package com.epam.aidial.cfg.configuration;

import com.epam.aidial.cfg.encryption.DataEncryptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
@ConditionalOnProperty(name = "datasource.vendor", havingValue = "H2")
public class H2Configuration {

    @Bean
    public DataSource dataSource(@Value("${h2.datasource.url}") String url,
                                 @Value("${h2.datasource.driver-class-name}") String driverClassName,
                                 @Value("${h2.datasource.username}") String username,
                                 @Value("${h2.datasource.masterKey}") String masterKey,
                                 @Value("${h2.datasource.encryptedFileKey}") String encryptedFileKey,
                                 @Value("${h2.datasource.password}") String dbPassword) {
        String filePassword = DataEncryptor.decryptedKeyBase64(masterKey, encryptedFileKey);
        return DataSourceBuilder.create()
                .driverClassName(driverClassName)
                .url(url)
                .username(username)
                .password(filePassword + " " + dbPassword)
                .build();
    }
}
