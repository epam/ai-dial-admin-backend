package com.epam.aidial.ql.model.filters.impl;

import com.epam.aidial.ql.model.Filter;
import com.epam.aidial.ql.model.filters.Not;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor(staticName = "of")
public class NotImpl implements Not {
    private Filter filter;
}
