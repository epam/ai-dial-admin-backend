package com.epam.aidial.cfg.dto.validation.validator;

import com.epam.aidial.cfg.dto.validation.annotation.Endpoint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

@Slf4j
public class EndpointValidator implements ConstraintValidator<Endpoint, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        log.trace("checking if value: {} is valid endpoint URL. context: {}", value, context);

        if (StringUtils.isEmpty(value)) {
            return true; // Allow null/empty - @NotBlank will handle empty validation
        }

        boolean isValid = com.epam.aidial.cfg.domain.validator.EndpointValidator.isValidUrl(value);
        if (!isValid) {
            log.trace("value: {} is invalid endpoint URL", value);
        }
        return isValid;
    }
}

