package com.epam.aidial.ql.model.impl;

import com.epam.aidial.expressions.Expression;
import com.epam.aidial.ql.model.LimitBy;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Singular;

import java.util.List;

@Data
@Builder
@AllArgsConstructor(staticName = "of")
public class LimitByImpl implements LimitBy {
    @Singular
    private List<Expression> expressions;
    private Long count;
}
