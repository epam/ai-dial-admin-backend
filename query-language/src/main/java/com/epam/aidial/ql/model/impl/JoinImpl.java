package com.epam.aidial.ql.model.impl;

import com.epam.aidial.expressions.Expression;
import com.epam.aidial.ql.common.model.enums.JoinStrictness;
import com.epam.aidial.ql.common.model.enums.JoinType;
import com.epam.aidial.ql.model.From;
import com.epam.aidial.ql.model.Join;
import lombok.Builder;
import lombok.Data;
import lombok.Singular;

import java.util.List;

@Data
@Builder
public class JoinImpl implements Join {
    private JoinStrictness strictness;
    private JoinType type;
    private From from;
    @Singular
    private List<Expression> leftExpressions;
    @Singular
    private List<Expression> rightExpressions;
}
