package com.epam.aidial.ql.dto;


import com.epam.aidial.expressions.enums.Type;

public class ColumnMetaDto {
    private String name;
    private Type type;

    public ColumnMetaDto() {
    }

    public ColumnMetaDto(String name, Type type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }
}
