package com.epam.aidial.cfg.service.core.validator;

import com.epam.aidial.cfg.domain.model.Key;
import com.epam.aidial.core.config.CoreKey;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Slf4j
@Component
public class CoreKeyValidator {

    public void validateUpdate(CoreKey coreKey, Key existingKey) {
        if (StringUtils.isBlank(coreKey.getKey())) {
            throw new IllegalArgumentException("Key value is required.");
        }

        if (!Objects.equals(coreKey.getKey(), existingKey.getKey())) {
            throw new IllegalArgumentException("Key value can not be changed.");
        }
    }
}
