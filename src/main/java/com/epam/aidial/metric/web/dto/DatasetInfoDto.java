package com.epam.aidial.metric.web.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DatasetInfoDto {
    private String name;
    private Long maxTimeRangeMs;
}
