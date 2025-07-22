package com.epam.aidial.cfg.dto;

public enum SourceTypeDto {
    ENDPOINTS("External endpoints"),
    TEMPLATE("Interceptor template"),
    CONTAINER("Interceptor container");

    private final String description;

    SourceTypeDto(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}