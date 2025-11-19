package com.epam.aidial.cfg.dto.validation.validator;

import com.epam.aidial.cfg.domain.validator.EndpointValidator;
import com.epam.aidial.cfg.dto.validation.annotation.RoutePath;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

@Slf4j
public class RoutePathValidator implements ConstraintValidator<RoutePath, String> {

    // Regex metacharacters that indicate the string is a regex pattern
    // These are characters that are not typically used in simple URL paths
    private static final Pattern REGEX_METACHARACTERS = Pattern.compile(".*[\\[\\]\\(\\)\\^\\$\\|\\{\\}\\\\].*");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        log.trace("checking if value: {} is valid route path. context: {}", value, context);

        // Empty string is valid
        if (StringUtils.isEmpty(value)) {
            return true;
        }

        // Blank/whitespace-only strings are invalid
        if (StringUtils.isBlank(value)) {
            return false;
        }

        // Check if string contains regex metacharacters
        boolean containsRegexSymbols = REGEX_METACHARACTERS.matcher(value).matches() ||
                value.contains("*") || value.contains("+") || value.contains("?");

        if (containsRegexSymbols) {
            // Validate as regex pattern
            try {
                Pattern.compile(value);
                return true;
            } catch (PatternSyntaxException e) {
                log.trace("value: {} is invalid regex pattern", value, e);
                return false;
            }
        } else {
            // Validate as relative URL path
            return EndpointValidator.isValidUrlPath(value);
        }
    }
}

