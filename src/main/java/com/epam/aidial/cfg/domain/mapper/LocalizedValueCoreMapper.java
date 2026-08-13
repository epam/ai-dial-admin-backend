package com.epam.aidial.cfg.domain.mapper;

import com.epam.aidial.cfg.domain.model.LocalizedValue;
import com.epam.aidial.core.config.CoreLocalizedValue;
import org.springframework.stereotype.Component;

@Component
public class LocalizedValueCoreMapper {

    public CoreLocalizedValue toCoreLocalizedValue(LocalizedValue domain) {
        if (domain == null) {
            return null;
        }
        return domain.isPlain() ? CoreLocalizedValue.of(domain.getPlainValue()) : CoreLocalizedValue.of(domain.getLocaleMap());
    }

    public LocalizedValue toLocalizedValue(CoreLocalizedValue coreLocalizedValue) {
        if (coreLocalizedValue == null) {
            return null;
        }
        return coreLocalizedValue.isPlain() ? LocalizedValue.of(coreLocalizedValue.getPlainValue()) : LocalizedValue.of(coreLocalizedValue.getLocaleMap());
    }
}
