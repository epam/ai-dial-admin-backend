package com.epam.aidial.ql.model.impl;


import com.epam.aidial.expressions.Expression;
import com.epam.aidial.ql.model.Tuple;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Singular;

import java.util.List;

@Data
@Builder
@AllArgsConstructor(staticName = "of")
public class TupleImpl implements Tuple {
    @Singular
    private List<Expression> expressions;
}
