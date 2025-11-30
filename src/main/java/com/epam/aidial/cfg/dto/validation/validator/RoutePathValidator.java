package com.epam.aidial.cfg.dto.validation.validator;

import com.epam.aidial.cfg.dto.validation.annotation.RoutePath;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

@Slf4j
public class RoutePathValidator implements ConstraintValidator<RoutePath, String> {

    private static final int MAX_LENGTH = 4096;

    // Pattern for valid plain paths: must start with /, only letters, digits, hyphens, underscores, dots, and slashes
    private static final Pattern PLAIN_PATH_PATTERN = Pattern.compile("^/[a-zA-Z0-9_\\-./]+$");

    // Core regex metacharacters: ()[]{}*+?.^$|\
    // Note: - is only special inside character classes [a-z]
    // Characters like &, <, >, =, !, : are NOT regex special characters
    private static final Pattern REGEX_METACHARACTERS = Pattern.compile(".*[()\\[\\]{}*+?.^$|\\\\].*");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        log.trace("checking if value: {} is valid route path. context: {}", value, context);

        // Reject null/empty strings
        if (StringUtils.isEmpty(value)) {
            return false;
        }

        // Reject whitespace-only strings
        if (StringUtils.isBlank(value)) {
            return false;
        }

        // Check max length
        if (value.length() > MAX_LENGTH) {
            log.trace("value: {} exceeds max length of {}", value.length(), MAX_LENGTH);
            return false;
        }

        // Check if string contains core regex metacharacters
        boolean isRegexPattern = REGEX_METACHARACTERS.matcher(value).matches();

        if (isRegexPattern) {
            // Validate as regex pattern
            return validateRegexPattern(value);
        } else {
            // Validate as plain path
            return validatePlainPath(value);
        }
    }

    private boolean validatePlainPath(String path) {
        // Must start with /
        if (!path.startsWith("/")) {
            log.trace("plain path: {} does not start with /", path);
            return false;
        }

        // Root path / is valid
        if (path.equals("/")) {
            return true;
        }

        // No consecutive slashes
        if (path.contains("//")) {
            log.trace("plain path: {} contains consecutive slashes", path);
            return false;
        }

        // Check pattern: only letters, digits, hyphens, underscores, dots, and slashes
        if (!PLAIN_PATH_PATTERN.matcher(path).matches()) {
            log.trace("plain path: {} does not match allowed pattern", path);
            return false;
        }

        return true;
    }

    private boolean validateRegexPattern(String pattern) {
        // Must start with / or ^/ or ^/+ for HTTP paths
        // ^/+ means ^ followed by one or more /, so ^/ covers the minimum case
        if (!pattern.startsWith("/") && !pattern.startsWith("^/")) {
            log.trace("regex pattern: {} does not start with / or ^/", pattern);
            return false;
        }

        // Check balanced brackets: (), [], {}
        if (!areBracketsBalanced(pattern)) {
            log.trace("regex pattern: {} has unbalanced brackets", pattern);
            return false;
        }

        // Must compile as valid Java regex
        try {
            Pattern.compile(pattern);
            return true;
        } catch (PatternSyntaxException e) {
            log.trace("regex pattern: {} is invalid", pattern, e);
            return false;
        }
    }

    private boolean areBracketsBalanced(String pattern) {
        Deque<Character> stack = new ArrayDeque<>();

        for (int i = 0; i < pattern.length(); i++) {
            char c = pattern.charAt(i);

            // Skip escaped characters
            if (c == '\\' && i + 1 < pattern.length()) {
                i++; // Skip the next character as it's escaped
                continue;
            }

            // Handle opening brackets
            if (c == '(' || c == '[' || c == '{') {
                stack.push(c);
            } else if (c == ')') { // Handle closing brackets
                if (stack.isEmpty() || stack.pop() != '(') {
                    return false;
                }
            } else if (c == ']') {
                if (stack.isEmpty() || stack.pop() != '[') {
                    return false;
                }
            } else if (c == '}') {
                if (stack.isEmpty() || stack.pop() != '{') {
                    return false;
                }
            }
        }

        return stack.isEmpty();
    }
}

