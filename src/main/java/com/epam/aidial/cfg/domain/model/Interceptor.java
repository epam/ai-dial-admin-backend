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
    private List<String> entities;
}
