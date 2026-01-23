package com.epam.aidial.cfg.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class Publication {

    private String path;
    private String requestName;
    private String folderId;
    private String author;
    private long createdAt;
    private PublicationStatus status;
    private List<Rule> rules;
    private List<PublicationMissingResource> missingResources;

    public abstract List<? extends PublicationResource> getResources();

}