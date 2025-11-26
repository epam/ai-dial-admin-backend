package com.epam.aidial.cfg.domain.model;

import lombok.Data;

import java.util.List;

@Data
public class Key {
    private String name;
    private String displayName;
    private String key;
    private String project;
    private boolean secured;
    private List<String> roles;
    private String description;
    private String projectContactPoint;
    private Long createdAt;
    private Long updatedAt;
    private Long expiresAt;
    private Long keyGeneratedAt;
    private ValidityState validityState;
}
