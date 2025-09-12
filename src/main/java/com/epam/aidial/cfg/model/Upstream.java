package com.epam.aidial.cfg.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Upstream {
    private String endpoint;
    private String key;
    private String extraData;
    private Integer weight;
    private Integer tier;
}
