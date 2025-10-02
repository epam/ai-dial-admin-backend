package com.epam.aidial.core.config;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class CoreRole {

    private String name;
    private Map<String, CoreLimit> limits = new HashMap<>();
    private CoreCostLimit costLimit; // 0.35.0
    private Map<String, CoreShareResourceLimit> share; // 0.30.0
}