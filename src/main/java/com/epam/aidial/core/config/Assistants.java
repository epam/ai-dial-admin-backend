package com.epam.aidial.core.config;

import java.util.HashMap;
import java.util.Map;

import lombok.Data;

@Data
public class Assistants {
    private String endpoint;
    private CoreFeatures features;
    private Map<String, CoreAssistant> assistants = new HashMap<>();
}