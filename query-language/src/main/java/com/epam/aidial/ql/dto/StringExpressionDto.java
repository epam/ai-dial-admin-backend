package com.epam.aidial.ql.dto;

import org.hibernate.validator.constraints.NotBlank;

public class StringExpressionDto implements ExpressionDto {
    @NotBlank
    private String expression;

    public StringExpressionDto() {
    }

    public StringExpressionDto(String expression) {
        this.expression = expression;
    }

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StringExpressionDto)) return false;

        StringExpressionDto that = (StringExpressionDto) o;

        return getExpression() != null ? getExpression().equals(that.getExpression()) : that.getExpression() == null;
    }

    @Override
    public int hashCode() {
        return getExpression() != null ? getExpression().hashCode() : 0;
    }

    @Override
    public String toString() {
        return "RequestedExpression{" +
                "expression='" + expression + '\'' +
                '}';
    }
}
