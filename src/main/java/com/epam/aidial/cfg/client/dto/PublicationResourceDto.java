package com.epam.aidial.cfg.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PublicationResourceDto {

    private PublicationResourceActionDto action;
    private String sourceUrl;
    private String targetUrl;
    private String reviewUrl;

}
