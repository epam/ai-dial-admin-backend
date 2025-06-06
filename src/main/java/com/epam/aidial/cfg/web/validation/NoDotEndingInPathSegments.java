package com.epam.aidial.cfg.web.validation;


import jakarta.validation.Constraint;
import jakarta.validation.constraints.NotNull;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Retention(RUNTIME)
@Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE})
@NotNull
@Constraint(validatedBy = {NoDotEndingInPathSegmentsValidator.class})
public @interface NoDotEndingInPathSegments {

    String message() default "Resource name and/or parent folders must not end with .(dot)";

    Class<?>[] groups() default {};

    Class<? extends jakarta.validation.Payload>[] payload() default {};

}
