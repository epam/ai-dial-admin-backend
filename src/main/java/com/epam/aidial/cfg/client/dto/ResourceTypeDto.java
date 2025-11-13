package com.epam.aidial.cfg.client.dto;

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
        return EnumUtils.getEnumIgnoreCase(ResourceTypeDto.class, value);
    }
}
