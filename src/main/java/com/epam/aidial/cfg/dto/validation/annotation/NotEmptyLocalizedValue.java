package com.epam.aidial.cfg.dto.validation.annotation;

import com.epam.aidial.cfg.dto.validation.validator.NotEmptyLocalizedValueValidator;
import jakarta.validation.Constraint;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Constraint(validatedBy = {NotEmptyLocalizedValueValidator.class})
@Documented
@Retention(RUNTIME)
@Target(FIELD)
public @interface NotEmptyLocalizedValue {

    String message() default "must not be empty";

    Class<?>[] groups() default {};

    Class<? extends jakarta.validation.Payload>[] payload() default {};

}
