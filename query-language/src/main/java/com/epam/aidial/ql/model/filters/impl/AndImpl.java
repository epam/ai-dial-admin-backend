package com.epam.aidial.ql.model.filters.impl;

import com.epam.aidial.ql.model.Filter;
import com.epam.aidial.ql.model.filters.And;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Singular;

import java.util.List;

@Data
@Builder
@AllArgsConstructor(staticName = "of")
public class AndImpl implements And {
    @Singular
    private List<Filter> filters;
}
