package com.epam.aidial.cfg.dto.validation.validator;

import com.epam.aidial.cfg.dto.validation.annotation.HttpMethod;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class HttpMethodValidator implements ConstraintValidator<HttpMethod, String> {

    private final Set<String> httpMethodNames;

    public HttpMethodValidator() {
        httpMethodNames = Arrays.stream(org.springframework.http.HttpMethod.values())
                .map(org.springframework.http.HttpMethod::name)
                .map(String::toUpperCase)
                .collect(Collectors.toSet());
    }

    @Override
    public boolean isValid(String method, ConstraintValidatorContext context) {
        if (StringUtils.isEmpty(method)) {
            log.debug("HTTP method is empty.");
            return false;
        }
        boolean isValid = httpMethodNames.contains(StringUtils.toRootUpperCase(method));
        if (!isValid) {
            log.debug("Invalid HTTP method: {}", method);
        }
        return isValid;
    }

}
