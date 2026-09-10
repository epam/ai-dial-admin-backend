package com.epam.aidial.cfg.model;

import lombok.Getter;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;

@Getter
public enum InterfaceType {

    CHAT("chat"),
    EMBEDDING("embedding"),
    MCP("mcp"),
    CUSTOM_UI("custom_ui"),
    ALL("all");

    private final String value;

    InterfaceType(String value) {
        this.value = value;
    }

    public static List<String> toParamValues(List<InterfaceType> types) {
        if (CollectionUtils.isEmpty(types) || types.contains(ALL)) {
            return null;
        }

        return types.stream()
                .map(InterfaceType::getValue)
                .toList();
    }
}