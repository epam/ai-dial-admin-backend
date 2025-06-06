package com.epam.aidial.ql.model.filters;

import com.epam.aidial.ql.model.Filter;

import java.util.List;

public interface Or extends Filter {
    List<Filter> getFilters();
}
