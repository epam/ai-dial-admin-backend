package com.epam.aidial.ql.dto;

import com.epam.aidial.ql.common.model.enums.SortDirection;
import jakarta.validation.constraints.NotNull;

/**
 * A class that defines a sort for a report request.
 * <p/>
 * If no groupings are defined, each report has its own default sorting, otherwise first grouping column is used as a default.
 */
public class SortDto {
    @NotNull
    private ExpressionDto expression;
    @NotNull
    private SortDirection direction;

    public SortDto() {
    }

    public SortDto(ExpressionDto expression, SortDirection direction) {
        this.expression = expression;
        this.direction = direction;
    }

    /**
     * Sorting expression.
     */
    public ExpressionDto getExpression() {
        return expression;
    }

    /**
     * {@link SortDto#getExpression()} ()}
     */
    public void setExpression(ExpressionDto expression) {
        this.expression = expression;
    }

    /**
     * Sort direction.
     */
    public SortDirection getDirection() {
        return direction;
    }

    /**
     * {@link SortDto#getDirection()}
     */
    public void setDirection(SortDirection direction) {
        this.direction = direction;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SortDto sort)) return false;

        if (getExpression() != null ? !getExpression().equals(sort.getExpression()) : sort.getExpression() != null)
            return false;
        return getDirection() == sort.getDirection();
    }

    @Override
    public int hashCode() {
        int result = getExpression() != null ? getExpression().hashCode() : 0;
        result = 31 * result + (getDirection() != null ? getDirection().hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Sort{" +
               "expression=" + expression +
               ", direction=" + direction +
               '}';
    }
}
