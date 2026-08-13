package com.epam.aidial.cfg.domain.validator;

import com.epam.aidial.cfg.domain.model.LocalizedValue;
import com.epam.aidial.cfg.exception.EntityAlreadyExistsException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;

/**
 * Validates that {@code displayName} is unique (per candidate with a matching {@code displayVersion})
 * among locales independently: a plain string and each locale of a localized value are only
 * compared against the same locale (or a plain value) of other candidates. There is no
 * {@code defaultLocale} fallback.
 */
@Component
public class DisplayNameUniquenessValidator {

    /**
     * @param currentEntityName             the entity being updated, excluded from the check; {@code null} on create
     * @param candidateDisplayNames         display names of other entities that already share {@code newDisplayVersion},
     *                                       keyed by entity name
     */
    public void validateUnique(String domainObjectType,
                               String currentEntityName,
                               LocalizedValue newDisplayName,
                               String newDisplayVersion,
                               Map<String, LocalizedValue> candidateDisplayNames) {
        if (isBlank(newDisplayName) && StringUtils.isEmpty(newDisplayVersion)) {
            return;
        }

        for (Map.Entry<String, LocalizedValue> entry : candidateDisplayNames.entrySet()) {
            if (entry.getKey().equals(currentEntityName)) {
                continue;
            }
            if (conflicts(newDisplayName, entry.getValue())) {
                throw new EntityAlreadyExistsException(domainObjectType + " with display name: '" + newDisplayName
                        + "' and display version: '" + newDisplayVersion + "' already exists");
            }
        }
    }

    private boolean conflicts(LocalizedValue left, LocalizedValue right) {
        if (left == null || right == null) {
            return false;
        }
        if (left.isPlain() && right.isPlain()) {
            return StringUtils.isNotEmpty(left.getPlainValue()) && Objects.equals(left.getPlainValue(), right.getPlainValue());
        }
        if (left.isPlain()) {
            return StringUtils.isNotEmpty(left.getPlainValue()) && right.getLocaleMap().containsValue(left.getPlainValue());
        }
        if (right.isPlain()) {
            return StringUtils.isNotEmpty(right.getPlainValue()) && left.getLocaleMap().containsValue(right.getPlainValue());
        }
        for (Map.Entry<String, String> entry : left.getLocaleMap().entrySet()) {
            String value = entry.getValue();
            if (StringUtils.isNotEmpty(value) && Objects.equals(value, right.getLocaleMap().get(entry.getKey()))) {
                return true;
            }
        }
        return false;
    }

    private boolean isBlank(LocalizedValue value) {
        if (value == null) {
            return true;
        }
        if (value.isPlain()) {
            return StringUtils.isBlank(value.getPlainValue());
        }
        return value.getLocaleMap() == null || value.getLocaleMap().values().stream().allMatch(StringUtils::isBlank);
    }
}
