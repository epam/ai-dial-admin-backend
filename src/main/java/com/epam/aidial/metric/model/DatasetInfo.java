package com.epam.aidial.metric.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DatasetInfo {
    private String name;
    private Long maxTimeRangeMs;
}
