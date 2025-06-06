package com.epam.aidial.ql.common.model.filters;

public interface Not<T> extends Filter<T> {
    Filter<T> getFilter();
}
