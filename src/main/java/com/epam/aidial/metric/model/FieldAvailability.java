package com.epam.aidial.metric.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FieldAvailability {
    private String name;
    private FieldType type;
    private List<?> availableValues;
}
