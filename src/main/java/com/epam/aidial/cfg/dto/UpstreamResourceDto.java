package com.epam.aidial.cfg.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpstreamResourceDto {

    private String endpoint;
    private String key;
    private String extraData;
    private int weight;
    private int tier;
}