package com.epam.aidial.ql.common.model.filters;

import java.util.List;

public interface And<T> extends Filter<T> {
    List<? extends Filter<T>> getFilters();
}
