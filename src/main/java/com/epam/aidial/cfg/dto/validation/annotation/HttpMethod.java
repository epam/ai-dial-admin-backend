package com.epam.aidial.cfg.dto.validation.annotation;

import com.epam.aidial.cfg.dto.validation.validator.HttpMethodValidator;
import jakarta.validation.Constraint;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Constraint(validatedBy = {HttpMethodValidator.class})
@Documented
@Retention(RUNTIME)
@Target({TYPE_USE, FIELD})
public @interface HttpMethod {

    String message() default "Invalid HTTP method.";

    Class<?>[] groups() default {};

    Class<? extends jakarta.validation.Payload>[] payload() default {};
}
