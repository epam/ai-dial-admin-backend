package com.epam.aidial.ql.model.impl;

import com.epam.aidial.expressions.Expression;
import com.epam.aidial.ql.model.Data;
import lombok.Builder;
import lombok.Singular;

import java.util.Collections;
import java.util.List;

@lombok.Data
@Builder
public class DataImpl implements Data {
    @Singular
    private List<Expression> expressions;
    @Builder.Default
    private List<List<Object>> data = Collections.emptyList();
}
