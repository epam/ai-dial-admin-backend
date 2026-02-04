package com.epam.aidial.cfg.client.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public abstract class BaseMetadataDto {

    private String name;
    private String parentPath;
    private String bucket;
    private String url;
    private NodeTypeDto nodeType;
    private String resourceType;
    private String author;
    private Long updatedAt;
    private String nextToken;
    private List<ResourceAccessTypeDto> permissions;

    public abstract List<? extends BaseMetadataDto> getItems();

}
