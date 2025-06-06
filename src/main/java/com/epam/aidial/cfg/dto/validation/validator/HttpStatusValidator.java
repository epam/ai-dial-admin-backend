package com.epam.aidial.cfg.dto.validation.validator;

import com.epam.aidial.cfg.dto.validation.annotation.HttpStatusCode;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HttpStatusValidator implements ConstraintValidator<HttpStatusCode, Integer> {

    @Override
    public boolean isValid(Integer status, ConstraintValidatorContext constraintValidatorContext) {
        try {
            org.springframework.http.HttpStatusCode.valueOf(status);
            return true;
        } catch (IllegalArgumentException exception) {
            log.debug("Invalid response status: {}", status);
            return false;
        }
    }
}
