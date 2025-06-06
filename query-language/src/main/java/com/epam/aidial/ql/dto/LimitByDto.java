package com.epam.aidial.ql.dto;

import jakarta.validation.constraints.Size;

import java.util.List;

public class LimitByDto {
    @Size(min = 1)
    private List<ExpressionDto> expressions;
    private long limit;

    public LimitByDto() {
    }

    public LimitByDto(List<ExpressionDto> expressions, long limit) {
        this.expressions = expressions;
        this.limit = limit;
    }

    public List<ExpressionDto> getExpressions() {
        return expressions;
    }

    public void setExpressions(List<ExpressionDto> expressions) {
        this.expressions = expressions;
    }

    public long getLimit() {
        return limit;
    }

    public void setLimit(long limit) {
        this.limit = limit;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LimitByDto limitBy)) return false;

        if (getLimit() != limitBy.getLimit()) return false;
        return getExpressions() != null ? getExpressions().equals(limitBy.getExpressions()) : limitBy.getExpressions() == null;
    }

    @Override
    public int hashCode() {
        int result = getExpressions() != null ? getExpressions().hashCode() : 0;
        result = 31 * result + (int) (getLimit() ^ (getLimit() >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "LimitBy{" +
                "expressions=" + expressions +
                ", limit=" + limit +
                '}';
    }
}
