package com.epam.aidial.cfg.domain.model;

import lombok.Data;

import java.util.List;

@Data
public class InterceptorRunner {

    private String name;
    private String displayName;
    private String description;
    private String completionEndpoint;
    private String configurationEndpoint;
    private List<String> interceptors;
}