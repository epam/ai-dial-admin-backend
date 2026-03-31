package com.epam.aidial.cfg.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpstreamResource {
    private String endpoint;
    private String responsesEndpoint;
    private String key;
    private String extraData;
    private Integer weight;
    private Integer tier;
}