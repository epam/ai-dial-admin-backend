package com.epam.aidial.ql.dto;

import org.hibernate.validator.constraints.NotBlank;

public class CalculatedColumnDto {
    @NotBlank
    private String name;
    @NotBlank
    private String expression;
    private String title;

    public String getName() {
        return name;
    }

    public CalculatedColumnDto setName(String name) {
        this.name = name;
        return this;
    }

    public String getExpression() {
        return expression;
    }

    public CalculatedColumnDto setExpression(String expression) {
        this.expression = expression;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public CalculatedColumnDto setTitle(String title) {
        this.title = title;
        return this;
    }
}
