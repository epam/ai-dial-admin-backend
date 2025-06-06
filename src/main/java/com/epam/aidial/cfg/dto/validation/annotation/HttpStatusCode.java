package com.epam.aidial.cfg.dto.validation.annotation;

import com.epam.aidial.cfg.dto.validation.validator.HttpStatusValidator;
import jakarta.validation.Constraint;
import jakarta.validation.constraints.NotNull;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Constraint(validatedBy = {HttpStatusValidator.class})
@Documented
@Retention(RUNTIME)
@NotNull
@Target(FIELD)
public @interface HttpStatusCode {

    String message() default "Invalid HTTP status code.";

    Class<?>[] groups() default {};

    Class<? extends jakarta.validation.Payload>[] payload() default {};
}
