package com.epam.aidial.cfg.domain.model;

public enum SourceType {
    ENDPOINTS("External endpoints"),
    TEMPLATE("Interceptor template"),
    CONTAINER("Interceptor container");

    private final String description;

    SourceType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}