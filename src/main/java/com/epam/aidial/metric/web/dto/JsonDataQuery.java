package com.epam.aidial.metric.web.dto;


import com.epam.aidial.ql.dto.CompletableDto;
import lombok.Data;

@Data
public class JsonDataQuery implements DataQuery {
    private CompletableDto query;
    private boolean fillGaps;
}
