package com.epam.aidial.ql.dto;


import jakarta.validation.constraints.NotNull;

import java.util.ArrayList;
import java.util.Collection;

public class TupleDto extends ArrayList<ExpressionDto> implements ExpressionDto {
    public TupleDto() {
    }

    public TupleDto(@NotNull Collection<? extends ExpressionDto> c) {
        super(c);
    }
}
