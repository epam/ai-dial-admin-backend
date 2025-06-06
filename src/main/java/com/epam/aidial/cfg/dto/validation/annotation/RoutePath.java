package com.epam.aidial.cfg.dto.validation.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Constraint(validatedBy = {})
@Documented
@Pattern(regexp = "^/(?=.{1,})([a-zA-Z0-9_-]+(?:/[a-zA-Z0-9_-]+)*/?)?$", message = "Invalid route path.")
@NotNull
@Retention(RUNTIME)
@Target({TYPE_USE, FIELD})
public @interface RoutePath {
    String message() default "Invalid route path.";

    Class<?>[] groups() default {};

    Class<? extends jakarta.validation.Payload>[] payload() default {};
}
