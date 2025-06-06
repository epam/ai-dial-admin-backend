package com.epam.aidial.cfg.configuration.datasource;

import com.zaxxer.hikari.HikariDataSource;
import lombok.RequiredArgsConstructor;

import java.util.function.Supplier;

@RequiredArgsConstructor
public class DynamicPasswordHikariDataSource extends HikariDataSource {

    private final Supplier<String> passwordProvider;

    @Override
    public String getPassword() {
        return passwordProvider.get();
    }

}
