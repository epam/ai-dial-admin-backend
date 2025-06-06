package com.epam.aidial.cfg.dto.validation.annotation;

import com.epam.aidial.cfg.dto.validation.validator.RoleLimitsValidator;
import jakarta.validation.Constraint;
import jakarta.validation.ReportAsSingleViolation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;

@Documented
@Constraint(validatedBy = {RoleLimitsValidator.class})
@Target({ElementType.TYPE, FIELD})
@Retention(RetentionPolicy.RUNTIME)
@ReportAsSingleViolation
public @interface RoleLimits {

    String message() default "The role limits cannot contain the default role name.";

    Class<?>[] groups() default {};

    Class<? extends jakarta.validation.Payload>[] payload() default {};
}
