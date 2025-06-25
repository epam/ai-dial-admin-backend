package com.epam.aidial.cfg.configuration.datasource;

import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.util.Credentials;
import lombok.RequiredArgsConstructor;

import java.util.function.Supplier;

@RequiredArgsConstructor
public class DynamicPasswordHikariDataSource extends HikariDataSource {

    private final Supplier<String> passwordProvider;

    @Override
    public Credentials getCredentials() {
        return Credentials.of(getUsername(), getPassword());
    }

    @Override
    public String getPassword() {
        return passwordProvider.get();
    }

}
