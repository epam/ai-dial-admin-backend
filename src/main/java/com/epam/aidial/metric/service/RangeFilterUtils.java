package com.epam.aidial.metric.service;

import com.epam.aidial.expressions.Column;
import com.epam.aidial.expressions.Expression;
import com.epam.aidial.expressions.impl.ConstantImpl;
import com.epam.aidial.ql.model.Filter;
import com.epam.aidial.ql.model.filters.And;
import com.epam.aidial.ql.model.filters.BinaryComparisonFilter;
import com.epam.aidial.ql.model.filters.Or;
import com.epam.aidial.ql.model.filters.impl.AndImpl;
import com.epam.aidial.ql.model.filters.impl.OrImpl;
import lombok.experimental.UtilityClass;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@UtilityClass
public class RangeFilterUtils {

    public static final String TIME_COLUMN = "_time";

    public static List<BinaryComparisonFilter> extractRangeFilter(Filter filter) {
        if (filter instanceof And and) {
            return and.getFilters().stream()
                    .filter(RangeFilterUtils::isRangeFilter)
                    .map(BinaryComparisonFilter.class::cast)
                    .toList();
        }

        if (isRangeFilter(filter)) {
            return List.of((BinaryComparisonFilter) filter);
        }

        return List.of();
    }

    public static Optional<Filter> extractNonRangeFilter(Filter filter) {
        if (filter == null) {
            return Optional.empty();
        }

        if (filter instanceof And and) {
            var nonRangeFilters = and.getFilters().stream().filter(f -> !isRangeFilter(f)).toList();
            if (nonRangeFilters.isEmpty()) {
                return Optional.empty();
            }
            if (nonRangeFilters.size() == 1) {
                return Optional.of(nonRangeFilters.get(0));
            }
            return Optional.of(AndImpl.of(nonRangeFilters));
        } else if (filter instanceof Or or) {
            var nonRangeFilters = or.getFilters().stream().filter(f -> !isRangeFilter(f)).toList();
            if (nonRangeFilters.isEmpty()) {
                return Optional.empty();
            }
            if (nonRangeFilters.size() == 1) {
                return Optional.of(nonRangeFilters.get(0));
            }
            return Optional.of(OrImpl.of(nonRangeFilters));
        }

        if (!isRangeFilter(filter)) {
            return Optional.of(filter);
        } else {
            return Optional.empty();
        }
    }

    public static boolean isRangeFilter(Filter filter) {
        if (filter instanceof BinaryComparisonFilter binaryComparisonFilter) {
            if (binaryComparisonFilter.getLeftExpression() instanceof Column leftColumn && leftColumn.getName().equals(TIME_COLUMN)) {
                return true;
            }
            if (binaryComparisonFilter.getRightExpression() instanceof Column rightColumn && rightColumn.getName().equals(TIME_COLUMN)) {
                return true;
            }
        }
        return false;
    }

    public static long getInstant(Expression expression) {
        if (!(expression instanceof ConstantImpl constant)) {
            throw new IllegalArgumentException("Only constant expressions are supported");
        }
        return (long) constant.getValue();
    }

    public static String convertInstantToString(long rawInstant) {
        var instant = Instant.ofEpochMilli(rawInstant);
        return instant.toString();
    }
}
