package com.epam.aidial.metric.web.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class TableSchemaDto {
    private List<ColumnDeclarationDto> columns;
}
