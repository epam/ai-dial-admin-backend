package com.epam.aidial.core.config.validation;

import static java.lang.annotation.ElementType.TYPE;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.ReportAsSingleViolation;

@Documented
@Constraint(validatedBy = { CustomApplicationsConformToTypeSchemasValidator.class})
@Target({ TYPE })
@Retention(RetentionPolicy.RUNTIME)
@ReportAsSingleViolation
public @interface CustomApplicationsConformToTypeSchemas {
    String message() default "Custom applications should comply with their schemas";
    Class<?>[] groups() default {};
    Class<? extends jakarta.validation.Payload>[] payload() default {};
}
