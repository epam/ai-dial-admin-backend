package com.epam.aidial.cfg.dto.validation.validator;

import com.epam.aidial.cfg.dto.validation.annotation.Regex;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

@Slf4j
public class RegexValidator implements ConstraintValidator<Regex, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        log.trace("checking if value: {} is valid regex pattern. context: {}", value, context);

        if (StringUtils.isEmpty(value)) {
            return true;
        }

        try {
            Pattern.compile(value);
            return true;
        } catch (PatternSyntaxException e) {
            log.trace("value: {} is invalid regex pattern", value, e);
            return false;
        }
    }
}