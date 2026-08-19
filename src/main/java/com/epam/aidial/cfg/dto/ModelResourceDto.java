package com.epam.aidial.cfg.dto;

import lombok.Data;

import java.util.Map;

@Data
public class ModelResourceDto {

    private String id;
    private String catalogSchemaId;
    private Map<String, Object> catalogProperties;
}
