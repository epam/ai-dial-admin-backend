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
public class Filter {
    private String columnId;
    private SearchFilterType operation;
    private List<Object> operands;
}
