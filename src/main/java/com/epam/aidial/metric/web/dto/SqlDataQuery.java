package com.epam.aidial.metric.web.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SqlDataQuery implements DataQuery {
    private String query;
    private boolean fillGaps;
}
