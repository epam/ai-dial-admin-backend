package com.epam.aidial.cfg.domain.model;

import com.epam.aidial.cfg.domain.model.source.InterceptorSource;
import com.epam.aidial.cfg.domain.value.LocalizedValue;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Data
public class Interceptor {

    private String name;
    private String endpoint;
    private Map<String, DeploymentInterface> interfaces;
    private String iconUrl;
    private LocalizedValue description;
    private Set<String> topics;
    private LocalizedValue displayName;
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