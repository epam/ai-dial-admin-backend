package com.epam.aidial.cfg.dao.converter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Converter
@Slf4j
public class StringListJsonConverter implements AttributeConverter<List<String>, String> {

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(List<String> attribute) {
        try {
            return attribute == null ? null : mapper.writeValueAsString(attribute);
        } catch (Exception e) {
            log.debug("Failed to convert list to data. Attribute value: {}", attribute, e);
            throw new IllegalStateException("Failed to convert list to data", e);
        }
    }

    @Override
    public List<String> convertToEntityAttribute(String data) {
        try {
            return data == null
                    ? new ArrayList<>()
                    : mapper.readValue(data, new TypeReference<>() {
                    });
        } catch (Exception e) {
            log.debug("Failed to convert data to list. Raw attribute value: {}", data, e);
            throw new IllegalStateException("Failed to convert data to list", e);

        }
    }
}