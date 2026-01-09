package com.epam.aidial.cfg.domain.model;

import com.epam.aidial.cfg.domain.model.source.InterceptorSource;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Data
public class Interceptor {

    private String name;
    private String endpoint;
    private String iconUrl;
    private String description;
    private Set<String> topics;
    private String displayName;
    private Boolean forwardAuthToken;
    private String author;
    private Long createdAt;
    private Long updatedAt;
    private Features features;
    private Map<String, Object> defaults;
    private List<String> dependencies;
    private List<String> entities;
    private List<String> applicationTypeSchemas;
    private InterceptorSource source;
}