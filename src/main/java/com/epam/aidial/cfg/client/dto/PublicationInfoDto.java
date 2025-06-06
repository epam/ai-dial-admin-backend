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
public class PublicationInfoDto {

    private String url;
    private String name;
    private String targetFolder;
    private String status;
    private long createdAt;
    private List<ResourceTypeDto> resourceTypes;
    private String author;

}
