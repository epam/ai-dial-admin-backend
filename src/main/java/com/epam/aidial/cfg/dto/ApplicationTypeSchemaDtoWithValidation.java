package com.epam.aidial.cfg.dto;

public record ApplicationTypeSchemaDtoWithValidation(ApplicationTypeSchemaDto schema, String message,
                                                     boolean isReadOnly) {
}