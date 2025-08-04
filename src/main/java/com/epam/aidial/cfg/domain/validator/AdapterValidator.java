package com.epam.aidial.cfg.domain.validator;

import com.epam.aidial.cfg.domain.model.Adapter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.regex.Pattern;

@Slf4j
@Component
public class AdapterValidator {

    private final IdFieldValidator idFieldValidator;

    private final String adapterNameValidationPattern;

    public AdapterValidator(IdFieldValidator idFieldValidator,
                            @Value("${validation.adapter.name:}") String adapterNameValidationPattern) {
        this.idFieldValidator = idFieldValidator;
        this.adapterNameValidationPattern = adapterNameValidationPattern;
    }

    public void validateAdapterCreation(Adapter adapter) {
        final String adapterName = adapter.getName();

        idFieldValidator.validateName(adapterName);

        if (StringUtils.isEmpty(adapterNameValidationPattern)) {
            log.debug("Adapter name validation pattern is empty, skipping validation for adapter: {}", adapterName);
            return;
        }

        if (!Pattern.matches(adapterNameValidationPattern, adapterName)) {
            throw new IllegalArgumentException("Adapter name '" + adapterName
                    + "' does not match the required pattern: " + adapterNameValidationPattern);
        }
    }

    public void validateUpdate(String adapterName, Adapter adapter) {
        if (!Objects.equals(adapterName, adapter.getName())) {
            throw new IllegalArgumentException("Adapter with name: '" + adapterName + "' can not be renamed. New adapter name: '" + adapter.getName() + "'");
        }
    }
}
