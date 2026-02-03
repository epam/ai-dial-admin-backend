package com.epam.aidial.cfg.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResourceMetadataRequest {
    private boolean recursive;
    private String path;
    private String nextToken;
    private Integer limit;
    private boolean permissions;
}
