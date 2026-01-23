package com.epam.aidial.cfg.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class PublicationDto {

    private String path;
    private String requestName;
    private String author;
    private long createdAt;
    private PublicationStatusDto status;
    private String folderId;
    private PublicationResourceActionDto action;
    private List<RuleDto> rules;
    private List<PublicationMissingResourceDto> missingResources;

}