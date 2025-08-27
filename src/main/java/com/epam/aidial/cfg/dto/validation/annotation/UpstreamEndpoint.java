package com.epam.aidial.cfg.dto.validation.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.constraints.Pattern;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Constraint(validatedBy = {})
@Pattern(regexp = "https?://[-a-zA-Z0-9@:%._\\+~#=]{1,256}(\\.[a-zA-Z0-9()]{1,6})?\\b(:[0-9]{1,5})?([-a-zA-Z0-9()@:%_\\+.~#?&//=]*)", message = "Invalid upstream endpoint")
@Documented
@Retention(RUNTIME)
@Target(FIELD)
public @interface UpstreamEndpoint {

    String message() default "Invalid upstream endpoint.";

    Class<?>[] groups() default {};

    Class<? extends jakarta.validation.Payload>[] payload() default {};

}
