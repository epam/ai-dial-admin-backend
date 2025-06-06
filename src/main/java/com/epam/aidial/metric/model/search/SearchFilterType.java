package com.epam.aidial.metric.model.search;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.collect.Maps;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@Getter
@RequiredArgsConstructor
public enum SearchFilterType {
    EQUALS("equals"),
    IN("in"),
    GREATER_THAN_OR_EQUAL("greaterThanOrEqual"),
    LESS_THAN("lessThan");


    private static final Map<String, SearchFilterType> lookup = Maps.newHashMap();

    static {
        for (SearchFilterType type : SearchFilterType.values()) {
            lookup.put(type.getValue().toLowerCase(), type);
        }
    }

    @JsonValue
    private final String value;

    @JsonCreator
    public static SearchFilterType getByValue(String value) {
        if (value == null) {
            return null;
        }
        return lookup.get(value.toLowerCase());
    }

}
