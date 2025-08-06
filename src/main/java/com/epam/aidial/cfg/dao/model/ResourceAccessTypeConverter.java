package com.epam.aidial.cfg.dao.model;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Converter
public class ResourceAccessTypeConverter implements AttributeConverter<Set<ResourceAccessType>, String> {

    private static final String DELIMITER = ",";

    @Override
    public String convertToDatabaseColumn(Set<ResourceAccessType> attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return null;
        }
        return attribute.stream()
                .map(Enum::name)
                .collect(Collectors.joining(DELIMITER));
    }

    @Override
    public Set<ResourceAccessType> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) {
            return new HashSet<>();
        }
        return Arrays.stream(dbData.split(DELIMITER))
                .map(ResourceAccessType::valueOf)
                .collect(Collectors.toSet());
    }
}
