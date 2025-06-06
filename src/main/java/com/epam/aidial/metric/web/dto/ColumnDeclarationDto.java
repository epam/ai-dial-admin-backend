package com.epam.aidial.metric.web.dto;

import com.epam.aidial.metric.model.configuration.ColumnType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ColumnDeclarationDto {
    private String name;
    private ColumnType type;
}
