package com.epam.aidial.cfg.dto.validation.validator;

import com.epam.aidial.cfg.domain.value.LocalizedValue;
import com.epam.aidial.cfg.dto.validation.annotation.NotEmptyLocalizedValue;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.commons.lang3.StringUtils;

public class NotEmptyLocalizedValueValidator implements ConstraintValidator<NotEmptyLocalizedValue, LocalizedValue> {

    @Override
    public boolean isValid(LocalizedValue value, ConstraintValidatorContext context) {
        if (value == null) {
            return false;
        }

        return value.isPlain()
                ? StringUtils.isNotBlank(value.getPlainValue())
                : value.getLocaleMap() != null
                && value.getLocaleMap().values().stream().anyMatch(StringUtils::isNotBlank);
    }
}
