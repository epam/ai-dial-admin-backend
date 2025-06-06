package com.epam.aidial.cfg.domain.validator;

import com.epam.aidial.cfg.dao.model.AdapterEntity;
import com.epam.aidial.cfg.dao.model.KeyEntity;
import com.epam.aidial.cfg.domain.model.Adapter;
import com.epam.aidial.cfg.domain.model.Key;
import com.epam.aidial.cfg.transaction.timestamp.TransactionTimestampContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@RequiredArgsConstructor
public class AdapterValidator {

    public void validateUpdate(String adapterName, Adapter adapter) {
        if (!Objects.equals(adapterName, adapter.getName())) {
            throw new IllegalArgumentException("Adapter with name: '" + adapterName + "' can not be renamed. New adapter name: '" + adapter.getName() + "'");
        }
    }
}
