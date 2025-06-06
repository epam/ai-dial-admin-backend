package com.epam.aidial.ql.dto;

import java.util.List;

public class DataDto {
    private List<ColumnMetaDto> meta;
    private List<List<Object>> data;

    public DataDto(List<ColumnMetaDto> meta, List<List<Object>> data) {
        this.meta = meta;
        this.data = data;
    }

    public List<ColumnMetaDto> getMeta() {
        return meta;
    }

    public List<List<Object>> getData() {
        return data;
    }
}
