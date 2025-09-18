package com.epam.aidial.cfg.exception;

import java.util.Collection;
import java.util.Map;

public class NotModifiedException extends RuntimeException {
    private final Map<String, Collection<String>> headers;

    public NotModifiedException(Map<String, Collection<String>> headers) {
        this.headers = headers;
    }

    public String getEtag() {
        var values = headers.get("etag");
        return (values == null || values.isEmpty())
                ? null
                : values.stream().findFirst().orElse(null);
    }
}
