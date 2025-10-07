package com.epam.aidial.cfg.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.apache.commons.lang3.EnumUtils;

public enum ResourceTypeDto {
    PROMPT,
    FILE,
    APPLICATION,
    CONVERSATION,
    TOOL_SET
    ;

    @JsonCreator
    public static ResourceTypeDto fromString(String value) {
        var enumValue = EnumUtils.getEnumIgnoreCase(ResourceTypeDto.class, value);
        if (enumValue == null) {
            throw new IllegalArgumentException("Invalid resource type: %s".formatted(value));
        }
        return enumValue;
    }
}
