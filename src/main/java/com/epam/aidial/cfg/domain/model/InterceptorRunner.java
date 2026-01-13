package com.epam.aidial.cfg.domain.model;

import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
public class InterceptorRunner {

    private String name;
    private String displayName;
    private String description;
    private String completionEndpoint;
    private String configurationEndpoint;
    private Long createdAt;
    private Long updatedAt;
    private List<String> interceptors;
    private Set<String> topics;
}