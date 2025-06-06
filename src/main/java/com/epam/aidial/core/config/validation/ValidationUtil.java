package com.epam.aidial.core.config.validation;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import java.util.Set;

public class ValidationUtil {

    private static final ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
    private static final Validator validator = validatorFactory.getValidator();

    public static <T> void validate(T obj) {
        Set<ConstraintViolation<T>> violations = validator.validate(obj);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
    }
}
