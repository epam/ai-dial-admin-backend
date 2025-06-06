package com.epam.aidial.metric.web.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class DataDto {
    private List<String> headers;
    private List<List<String>> data;
}
