package com.epam.aidial.ql.dto;


import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public class QueryDto implements CompletableDto {
    private boolean distinct;

    @Size(min = 1)
    private List<ExpressionDto> expressions;

    @NotNull
    private FromDto from;

    @Nullable
    private JoinDto join;

    @Nullable
    private FilterDto preScale;

    @Nullable
    private FilterDto where;

    private List<ExpressionDto> groupBy = Collections.emptyList();

    private boolean withTotals;

    @Nullable
    private FilterDto having;

    private List<SortDto> orderBy = Collections.emptyList();

    @Nullable
    private LimitByDto limitBy;

    @Nullable
    private Long offset;

    @Nullable
    private Long limit;

    public boolean isDistinct() {
        return distinct;
    }

    public void setDistinct(boolean distinct) {
        this.distinct = distinct;
    }

    public List<ExpressionDto> getExpressions() {
        return expressions;
    }

    public void setExpressions(List<ExpressionDto> expressions) {
        this.expressions = expressions;
    }

    public FromDto getFrom() {
        return from;
    }

    public void setFrom(FromDto from) {
        this.from = from;
    }

    @Nullable
    public JoinDto getJoin() {
        return join;
    }

    public void setJoin(@Nullable JoinDto join) {
        this.join = join;
    }

    public FilterDto getPreScale() {
        return preScale;
    }

    public void setPreScale(FilterDto preScale) {
        this.preScale = preScale;
    }

    @Nullable
    public FilterDto getWhere() {
        return where;
    }

    public void setWhere(@Nullable FilterDto where) {
        this.where = where;
    }

    public List<ExpressionDto> getGroupBy() {
        return groupBy;
    }

    public void setGroupBy(List<ExpressionDto> groupBy) {
        this.groupBy = groupBy;
    }

    public boolean isWithTotals() {
        return withTotals;
    }

    public void setWithTotals(boolean withTotals) {
        this.withTotals = withTotals;
    }

    @Nullable
    public FilterDto getHaving() {
        return having;
    }

    public void setHaving(@Nullable FilterDto having) {
        this.having = having;
    }

    public List<SortDto> getOrderBy() {
        return orderBy;
    }

    public void setOrderBy(List<SortDto> orderBy) {
        this.orderBy = orderBy;
    }

    @Nullable
    public LimitByDto getLimitBy() {
        return limitBy;
    }

    public void setLimitBy(@Nullable LimitByDto limitBy) {
        this.limitBy = limitBy;
    }

    @Nullable
    public Long getOffset() {
        return offset;
    }

    public void setOffset(@Nullable Long offset) {
        this.offset = offset;
    }

    @Nullable
    public Long getLimit() {
        return limit;
    }

    public void setLimit(@Nullable Long limit) {
        this.limit = limit;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof QueryDto query)) return false;

        if (isDistinct() != query.isDistinct()) return false;
        if (isWithTotals() != query.isWithTotals()) return false;
        if (getExpressions() != null ? !getExpressions().equals(query.getExpressions()) : query.getExpressions() != null)
            return false;
        if (getFrom() != null ? !getFrom().equals(query.getFrom()) : query.getFrom() != null) return false;
        if (getJoin() != null ? !getJoin().equals(query.getJoin()) : query.getJoin() != null) return false;
        if (getWhere() != null ? !getWhere().equals(query.getWhere()) : query.getWhere() != null) return false;
        if (getGroupBy() != null ? !getGroupBy().equals(query.getGroupBy()) : query.getGroupBy() != null) return false;
        if (getHaving() != null ? !getHaving().equals(query.getHaving()) : query.getHaving() != null) return false;
        if (getOrderBy() != null ? !getOrderBy().equals(query.getOrderBy()) : query.getOrderBy() != null) return false;
        if (getLimitBy() != null ? !getLimitBy().equals(query.getLimitBy()) : query.getLimitBy() != null) return false;
        if (getOffset() != null ? !getOffset().equals(query.getOffset()) : query.getOffset() != null) return false;
        return getLimit() != null ? getLimit().equals(query.getLimit()) : query.getLimit() == null;
    }

    @Override
    public int hashCode() {
        int result = (isDistinct() ? 1 : 0);
        result = 31 * result + (getExpressions() != null ? getExpressions().hashCode() : 0);
        result = 31 * result + (getFrom() != null ? getFrom().hashCode() : 0);
        result = 31 * result + (getJoin() != null ? getJoin().hashCode() : 0);
        result = 31 * result + (getWhere() != null ? getWhere().hashCode() : 0);
        result = 31 * result + (getGroupBy() != null ? getGroupBy().hashCode() : 0);
        result = 31 * result + (isWithTotals() ? 1 : 0);
        result = 31 * result + (getHaving() != null ? getHaving().hashCode() : 0);
        result = 31 * result + (getOrderBy() != null ? getOrderBy().hashCode() : 0);
        result = 31 * result + (getLimitBy() != null ? getLimitBy().hashCode() : 0);
        result = 31 * result + (getOffset() != null ? getOffset().hashCode() : 0);
        result = 31 * result + (getLimit() != null ? getLimit().hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Query{" +
                "distinct=" + distinct +
                ", expressions=" + expressions +
                ", from=" + from +
                ", join=" + join +
                ", where=" + where +
                ", groupBy=" + groupBy +
                ", withTotals=" + withTotals +
                ", having=" + having +
                ", orderBy=" + orderBy +
                ", limitBy=" + limitBy +
                ", offset=" + offset +
                ", limit=" + limit +
                '}';
    }
}
