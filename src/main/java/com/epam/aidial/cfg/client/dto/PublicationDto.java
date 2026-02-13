package com.epam.aidial.cfg.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PublicationDto {

    private String url;
    private String name;
    private String targetFolder;
    private PublicationStatusDto status;
    private long createdAt;
    private List<PublicationResourceDto> resources;
    private List<RuleDto> rules;
    private List<ResourceTypeDto> resourceTypes;
    private String author;
    private String displayAuthor;

}