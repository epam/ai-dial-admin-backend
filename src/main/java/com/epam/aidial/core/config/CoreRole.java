package com.epam.aidial.core.config;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class CoreRole {

    public static final String DEFAULT_ROLE_NAME = "default";

    private String name;
    private Map<String, CoreLimit> limits = new HashMap<>();
}