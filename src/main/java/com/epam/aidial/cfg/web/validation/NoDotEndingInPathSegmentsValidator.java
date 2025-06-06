package com.epam.aidial.cfg.web.validation;

import com.epam.aidial.cfg.utils.PathUtils;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class NoDotEndingInPathSegmentsValidator implements ConstraintValidator<NoDotEndingInPathSegments, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return !PathUtils.isAnyPathSegmentEndsWithDot(value);
    }

}
