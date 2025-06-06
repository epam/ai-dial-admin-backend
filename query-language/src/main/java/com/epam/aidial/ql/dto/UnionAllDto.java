package com.epam.aidial.ql.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Size;

import java.util.List;

public class UnionAllDto implements CompletableDto {
    @JsonProperty("unionAll")
    @Size(min = 1)
    private List<CompletableDto> queries;

    public UnionAllDto() {
    }

    public UnionAllDto(List<CompletableDto> queries) {
        this.queries = queries;
    }

    public List<CompletableDto> getQueries() {
        return queries;
    }

    public void setQueries(List<CompletableDto> queries) {
        this.queries = queries;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UnionAllDto unionAll)) return false;

        return getQueries() != null ? getQueries().equals(unionAll.getQueries()) : unionAll.getQueries() == null;
    }

    @Override
    public int hashCode() {
        return getQueries() != null ? getQueries().hashCode() : 0;
    }

    @Override
    public String toString() {
        return "UnionAll{" +
                "queries=" + queries +
                '}';
    }
}
