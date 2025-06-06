package com.epam.aidial.cfg.dto;

import lombok.Data;

import java.util.List;

@Data
public class PublicationInfoDto {

    private String path;
    private String requestName;
    private String author;
    private long createdAt;
    private List<ResourceTypeDto> resourceTypes;

}
