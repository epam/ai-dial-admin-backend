package com.epam.aidial.cfg.domain.service;

import com.epam.aidial.cfg.dao.audit.jpa.ConfigRevisionJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class DatabaseService {

    private final ConfigRevisionJpaRepository configRevisionJpaRepository;

    @Transactional(readOnly = true)
    public boolean isInitializedEmptyDatabase() {
        // Initialized empty database is database which contains only predefined entities saved during DB migration:
        // 1. 'default' role and corresponding record in revinfo table.
        // Once new default entities are added to the database (along with record in revinfo table), the list above
        // and check below should be adjusted
        return configRevisionJpaRepository.count() == 1;
    }

}
