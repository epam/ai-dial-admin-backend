package com.epam.aidial.core.config;

import lombok.Data;

import java.util.List;

@Data
public class CoreKey {

    private String project;
    private String role;
    private boolean secured;
    private List<String> roles;
}
