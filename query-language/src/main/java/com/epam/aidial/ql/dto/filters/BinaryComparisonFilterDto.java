package com.epam.aidial.ql.dto.filters;

import com.epam.aidial.ql.common.model.filters.BinaryComparisonFilter;
import com.epam.aidial.ql.dto.FilterDto;
import com.epam.aidial.ql.dto.ExpressionDto;
import com.epam.aidial.ql.common.model.enums.BinaryComparisonOperator;

public class BinaryComparisonFilterDto implements FilterDto, BinaryComparisonFilter<ExpressionDto> {
    private ExpressionDto left;
    private BinaryComparisonOperator operator;
    private ExpressionDto right;

    public BinaryComparisonFilterDto() {
    }

    public BinaryComparisonFilterDto(ExpressionDto left, BinaryComparisonOperator operator, ExpressionDto right) {
        this.left = left;
        this.operator = operator;
        this.right = right;
    }

    public ExpressionDto getLeft() {
        return left;
    }

    public void setLeft(ExpressionDto left) {
        this.left = left;
    }

    public BinaryComparisonOperator getOperator() {
        return operator;
    }

    public void setOperator(BinaryComparisonOperator operator) {
        this.operator = operator;
    }

    public ExpressionDto getRight() {
        return right;
    }

    public void setRight(ExpressionDto right) {
        this.right = right;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BinaryComparisonFilterDto)) return false;

        BinaryComparisonFilterDto that = (BinaryComparisonFilterDto) o;

        if (getLeft() != null ? !getLeft().equals(that.getLeft()) : that.getLeft() != null) return false;
        if (getOperator() != that.getOperator()) return false;
        return getRight() != null ? getRight().equals(that.getRight()) : that.getRight() == null;
    }

    @Override
    public int hashCode() {
        int result = getLeft() != null ? getLeft().hashCode() : 0;
        result = 31 * result + (getOperator() != null ? getOperator().hashCode() : 0);
        result = 31 * result + (getRight() != null ? getRight().hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "BinaryComparisonFilter{" +
                "left=" + left +
                ", operator=" + operator +
                ", right=" + right +
                '}';
    }
}
