package com.epam.aidial.cfg.dto.validation.validator;

import com.epam.aidial.cfg.dto.LimitDto;
import com.epam.aidial.cfg.dto.validation.annotation.RoleLimits;
import com.epam.aidial.core.config.CoreRole;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.commons.collections4.MapUtils;

import java.util.Map;

public class RoleLimitsValidator implements ConstraintValidator<RoleLimits, Map<String, LimitDto>> {

    @Override
    public boolean isValid(Map<String, LimitDto> roleLimits, ConstraintValidatorContext constraintValidatorContext) {
        if (MapUtils.isEmpty(roleLimits)) {
            return true;
        }
        return !roleLimits.containsKey(CoreRole.DEFAULT_ROLE_NAME);
    }
}

