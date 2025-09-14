package com.epam.aidial.cfg.utils;

import lombok.experimental.UtilityClass;

import java.util.Map;

@UtilityClass
public class HeaderUtils {
    public static final String IF_MATCH_HEADER_NAME = "If-Match";
    public static final String IF_NONE_MATCH_HEADER_NAME = "If-None-Match";

    public static Map<String, String> createHeadersForCreate(boolean allowOverride, String etag) {
        if (!allowOverride) {
            return Map.of(IF_NONE_MATCH_HEADER_NAME, "*");
        }
        return createIfMatchHeaders(etag);
    }

    public static Map<String, String> createIfMatchHeaders(String etag) {
        if (etag != null) {
            return Map.of(IF_MATCH_HEADER_NAME, etag);
        }
        return Map.of();
    }
}
