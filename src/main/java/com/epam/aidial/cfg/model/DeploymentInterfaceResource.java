package com.epam.aidial.cfg.model;

import lombok.Data;

import java.util.Map;

@Data
public class DeploymentInterfaceResource {

    private String baseUrl;
    private Map<String, String> defaultHeaders;
    private InterfaceModeResource mode;
    private FeaturesResource features;
}
