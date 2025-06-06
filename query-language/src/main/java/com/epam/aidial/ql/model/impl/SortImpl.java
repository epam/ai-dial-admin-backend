package com.epam.aidial.ql.model.impl;

import com.epam.aidial.expressions.Expression;
import com.epam.aidial.ql.common.model.enums.SortDirection;
import com.epam.aidial.ql.model.Sort;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor(staticName = "of")
public class SortImpl implements Sort {
    private Expression expression;
    private SortDirection direction;
}
