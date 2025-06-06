package com.epam.aidial.ql.dto.filters;

import com.epam.aidial.ql.common.model.enums.UnaryComparisonOperator;
import com.epam.aidial.ql.common.model.filters.UnaryComparisonFilter;
import com.epam.aidial.ql.dto.ExpressionDto;
import com.epam.aidial.ql.dto.FilterDto;

public class UnaryComparisonFilterDto implements FilterDto, UnaryComparisonFilter<ExpressionDto> {
    private ExpressionDto expression;
    private UnaryComparisonOperator operator;

    public UnaryComparisonFilterDto() {
    }

    public UnaryComparisonFilterDto(ExpressionDto expression, UnaryComparisonOperator operator) {
        this.expression = expression;
        this.operator = operator;
    }

    public ExpressionDto getExpression() {
        return expression;
    }

    public void setExpression(ExpressionDto expression) {
        this.expression = expression;
    }

    public UnaryComparisonOperator getOperator() {
        return operator;
    }

    public void setOperator(UnaryComparisonOperator operator) {
        this.operator = operator;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UnaryComparisonFilterDto)) return false;

        UnaryComparisonFilterDto that = (UnaryComparisonFilterDto) o;

        if (getExpression() != null ? !getExpression().equals(that.getExpression()) : that.getExpression() != null)
            return false;
        return getOperator() == that.getOperator();
    }

    @Override
    public int hashCode() {
        int result = getExpression() != null ? getExpression().hashCode() : 0;
        result = 31 * result + (getOperator() != null ? getOperator().hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "UnaryComparisonFilter{" +
                "expression=" + expression +
                ", operator=" + operator +
                '}';
    }
}
