package com.epam.aidial.cfg.domain.model;

public record ApplicationTypeSchemaWithValidation(ApplicationTypeSchema schema, String message, boolean isReadOnly) {
}