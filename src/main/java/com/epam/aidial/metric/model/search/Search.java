package com.epam.aidial.metric.model.search;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Search {
    private Integer pageSize;
    private Integer pageNumber;
    private List<Filter> filters;
    //todo: add sorts
    //todo: add aggregation
}
