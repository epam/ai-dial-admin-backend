package com.epam.aidial.cfg.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class PublicationResourceIssue {

    private ResourceType resourceType;
    private String path;
    private String message;
}