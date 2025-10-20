package com.epam.aidial.cfg.domain.validator;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class IdFieldValidator {

    private static final Pattern NAME_ILLEGAL_CHARACTERS_PATTERN = Pattern.compile("^[^%;/\\\\]*$");

    public void validateName(String domainObjectType, String name) {
        validateId(domainObjectType, name, "name");

        Matcher matcher = NAME_ILLEGAL_CHARACTERS_PATTERN.matcher(name);

        if (!matcher.matches()) {
            throw new IllegalArgumentException("Illegal characters found in %s name: %s".formatted(domainObjectType, name));
        }
    }

    public void validateId(String domainObjectType, Object id, String idFieldName) {
        if (id == null) {
            throw new IllegalArgumentException("%s %s must not be empty".formatted(domainObjectType, idFieldName));
        }

        if (id instanceof String stringId && StringUtils.isBlank(stringId)) {
            throw new IllegalArgumentException("%s %s must not be empty".formatted(domainObjectType, idFieldName));
        }
    }
}
