package com.epam.aidial.cfg.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
public class PublicationResourceDto {
    private String sourceUrl;
    private String targetUrl;
    private String reviewUrl;
    private PublicationResourceActionDto action;

}