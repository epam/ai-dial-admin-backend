package com.epam.aidial.cfg.service;

import com.epam.aidial.cfg.client.DeploymentClient;
import com.epam.aidial.cfg.configuration.logging.LogExecution;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@LogExecution
public class CoreDeploymentService {

    private final DeploymentClient deploymentClient;

    public Map<String, Object> getConfiguration(String deploymentName) {
        return deploymentClient.getConfiguration(deploymentName);
    }
}
