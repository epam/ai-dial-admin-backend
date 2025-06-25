package com.epam.aidial.cfg.domain.model;

import lombok.Data;

import java.util.List;

@Data
public class Interceptor {

    private String name;
    private String endpoint;
    private String iconUrl;
    private String description;
    private String displayName;
    private Boolean forwardAuthToken;
    private String author;
    private Long createdAt;
    private Long updatedAt;
    private List<String> dependencies;
    private List<String> entities;
    private String interceptorRunner;
    private String configurationEndpoint;
}
