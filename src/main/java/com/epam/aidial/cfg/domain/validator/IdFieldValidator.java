package com.epam.aidial.cfg.domain.validator;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class IdFieldValidator {

    private static final Set<Character> NAME_ILLEGAL_CHARACTERS = Set.of('%', ';', '/', '\\');

    public void validateName(String domainObjectType, String name) {
        validateId(domainObjectType, name, "name");

        for (var character : name.toCharArray()) {
            if (NAME_ILLEGAL_CHARACTERS.contains(character)) {
                throw new IllegalArgumentException("Illegal character '%s' found in %s name: %s".formatted(character, domainObjectType, name));
            }
        }
    }

    public void validateId(String domainObjectClassName, Object id, String idFieldName) {
        if (id == null) {
            throw new IllegalArgumentException("%s %s must not be empty".formatted(domainObjectClassName, idFieldName));
        }

        if (id instanceof String stringId && StringUtils.isBlank(stringId)) {
            throw new IllegalArgumentException("%s %s must not be empty".formatted(domainObjectClassName, idFieldName));
        }
    }
}
