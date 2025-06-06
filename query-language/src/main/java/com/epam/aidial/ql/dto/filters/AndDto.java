package com.epam.aidial.ql.dto.filters;

import com.epam.aidial.ql.common.model.filters.And;
import com.epam.aidial.ql.dto.ExpressionDto;
import com.epam.aidial.ql.dto.FilterDto;
import jakarta.validation.constraints.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class AndDto extends ArrayList<FilterDto> implements FilterDto, And<ExpressionDto> {
    public AndDto() {
    }

    public AndDto(@NotNull Collection<? extends FilterDto> c) {
        super(c);
    }

    @Override
    public List<FilterDto> getFilters() {
        return this;
    }
}
