package com.epam.aidial.cfg.domain.validator;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class IdFieldValidator {

    private static final Pattern NAME_LEGAL_CHARACTERS_PATTERN = Pattern.compile("^[A-Za-z0-9-_.,:;<>(){}\\[\\] ]+$");

    public void validateName(String name) {
        validateId(name, "name");

        Matcher matcher = NAME_LEGAL_CHARACTERS_PATTERN.matcher(name);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Illegal characters found in name");
        }
    }

    public void validateId(Object id, String idFieldName) {
        if (id == null) {
            throw new IllegalArgumentException("%s must not be empty".formatted(idFieldName));
        }

        if (id instanceof String stringId && StringUtils.isBlank(stringId)) {
            throw new IllegalArgumentException("%s must not be empty".formatted(idFieldName));
        }
    }
}
