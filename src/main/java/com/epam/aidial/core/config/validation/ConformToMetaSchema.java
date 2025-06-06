package com.epam.aidial.core.config.validation;

import static java.lang.annotation.ElementType.FIELD;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.ReportAsSingleViolation;

@Documented
@Constraint(validatedBy = { ConformToMetaSchemaValidator.class})
@Target({ FIELD })
@Retention(RetentionPolicy.RUNTIME)
@ReportAsSingleViolation
public @interface ConformToMetaSchema {
    String message() default "Schemas should comply with the meta schema";
    Class<?>[] groups() default {};
    Class<? extends jakarta.validation.Payload>[] payload() default {};
}


