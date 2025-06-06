package com.epam.aidial.ql.dto;

import com.epam.aidial.ql.common.model.enums.JoinStrictness;
import com.epam.aidial.ql.common.model.enums.JoinType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public class JoinDto {
    @NotNull
    private JoinStrictness strictness;
    @NotNull
    private JoinType type;
    @NotNull
    private FromDto from;
    @Size(min = 1)
    private List<ExpressionDto> left;
    @Size(min = 1)
    private List<ExpressionDto> right;

    public JoinDto() {
    }

    public JoinDto(JoinStrictness strictness, JoinType type, FromDto from, List<ExpressionDto> left, List<ExpressionDto> right) {
        this.strictness = strictness;
        this.type = type;
        this.from = from;
        this.left = left;
        this.right = right;
    }

    public JoinStrictness getStrictness() {
        return strictness;
    }

    public void setStrictness(JoinStrictness strictness) {
        this.strictness = strictness;
    }

    public JoinType getType() {
        return type;
    }

    public void setType(JoinType type) {
        this.type = type;
    }

    public FromDto getFrom() {
        return from;
    }

    public void setFrom(FromDto from) {
        this.from = from;
    }

    public List<ExpressionDto> getLeft() {
        return left;
    }

    public void setLeft(List<ExpressionDto> left) {
        this.left = left;
    }

    public List<ExpressionDto> getRight() {
        return right;
    }

    public void setRight(List<ExpressionDto> right) {
        this.right = right;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof JoinDto join)) return false;

        if (getStrictness() != join.getStrictness()) return false;
        if (getType() != join.getType()) return false;
        if (getFrom() != null ? !getFrom().equals(join.getFrom()) : join.getFrom() != null) return false;
        if (getLeft() != null ? !getLeft().equals(join.getLeft()) : join.getLeft() != null) return false;
        return getRight() != null ? getRight().equals(join.getRight()) : join.getRight() == null;
    }

    @Override
    public int hashCode() {
        int result = getStrictness() != null ? getStrictness().hashCode() : 0;
        result = 31 * result + (getType() != null ? getType().hashCode() : 0);
        result = 31 * result + (getFrom() != null ? getFrom().hashCode() : 0);
        result = 31 * result + (getLeft() != null ? getLeft().hashCode() : 0);
        result = 31 * result + (getRight() != null ? getRight().hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Join{" +
                "strictness=" + strictness +
                ", type=" + type +
                ", from=" + from +
                ", left=" + left +
                ", right=" + right +
                '}';
    }
}
