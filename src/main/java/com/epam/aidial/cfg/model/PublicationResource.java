package com.epam.aidial.cfg.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class PublicationResource {

    private PublicationResourceAction action;
    private String sourceUrl;
    private String reviewUrl;
    private String targetUrl;

}