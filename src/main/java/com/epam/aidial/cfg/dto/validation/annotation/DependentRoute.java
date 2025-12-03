package com.epam.aidial.cfg.dto.validation.annotation;

import com.epam.aidial.cfg.dto.validation.validator.DependentRouteValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Constraint(validatedBy = DependentRouteValidator.class)
@Documented
@Retention(RUNTIME)
@Target(TYPE)
public @interface DependentRoute {

    String message() default "Invalid dependent route configuration";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}