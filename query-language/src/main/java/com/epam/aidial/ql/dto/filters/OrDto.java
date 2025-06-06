package com.epam.aidial.ql.dto.filters;

import com.epam.aidial.ql.common.model.filters.Or;
import com.epam.aidial.ql.dto.ExpressionDto;
import com.epam.aidial.ql.dto.FilterDto;
import jakarta.validation.constraints.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class OrDto extends ArrayList<FilterDto> implements FilterDto, Or<ExpressionDto> {
    public OrDto() {
    }

    public OrDto(@NotNull Collection<? extends FilterDto> c) {
        super(c);
    }

    @Override
    public List<FilterDto> getFilters() {
        return this;
    }
}
