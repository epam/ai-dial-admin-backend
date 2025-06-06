package com.epam.aidial.ql.model.filters.impl;

import com.epam.aidial.ql.model.Filter;
import com.epam.aidial.ql.model.filters.Or;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Singular;

import java.util.List;

@Data
@Builder
@AllArgsConstructor(staticName = "of")
public class OrImpl implements Or {
    @Singular
    private List<Filter> filters;
}
