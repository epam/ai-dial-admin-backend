package com.epam.aidial.ql.dto;


import jakarta.validation.constraints.NotNull;

public class TableDto implements FromDto {
    @NotNull
    private String name;

    public TableDto() {
    }

    public TableDto(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TableDto report)) return false;

        return getName() != null ? getName().equals(report.getName()) : report.getName() == null;
    }

    @Override
    public int hashCode() {
        return getName() != null ? getName().hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Report{" +
                "name='" + name + '\'' +
                '}';
    }
}
