package com.epam.aidial.cfg.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpstreamDto {

    private String endpoint;
    private String key;
    private String extraData;
    private String secretExtraData;
    private Integer weight;
    private Integer tier;
}