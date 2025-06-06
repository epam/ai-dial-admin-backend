package com.epam.aidial.ql.model.filters;

import com.epam.aidial.ql.model.Filter;

import java.util.List;

public interface And extends Filter {
    List<Filter> getFilters();
}
