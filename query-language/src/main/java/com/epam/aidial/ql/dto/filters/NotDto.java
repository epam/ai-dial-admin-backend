package com.epam.aidial.ql.dto.filters;

import com.epam.aidial.ql.common.model.filters.Not;
import com.epam.aidial.ql.dto.ExpressionDto;
import com.epam.aidial.ql.dto.FilterDto;

public class NotDto implements FilterDto, Not<ExpressionDto> {
    private FilterDto filter;

    public NotDto() {
    }

    public NotDto(FilterDto filter) {
        this.filter = filter;
    }

    public FilterDto getFilter() {
        return filter;
    }

    public void setFilter(FilterDto filter) {
        this.filter = filter;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NotDto)) return false;

        NotDto not = (NotDto) o;

        return getFilter() != null ? getFilter().equals(not.getFilter()) : not.getFilter() == null;
    }

    @Override
    public int hashCode() {
        return getFilter() != null ? getFilter().hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Not{" +
                "filter=" + filter +
                '}';
    }
}
