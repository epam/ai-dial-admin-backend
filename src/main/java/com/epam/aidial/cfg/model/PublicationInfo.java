package com.epam.aidial.cfg.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PublicationInfo {

    private String path;
    private String requestName;
    private String author;
    private long createdAt;
    private List<ResourceType> resourceTypes;

}
