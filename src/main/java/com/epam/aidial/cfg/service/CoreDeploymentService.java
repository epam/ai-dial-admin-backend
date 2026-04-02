package com.epam.aidial.cfg.service;

import com.epam.aidial.cfg.client.DeploymentClient;
import com.epam.aidial.cfg.client.dto.ToolSetDataDto;
import com.epam.aidial.cfg.client.mapper.DeploymentClientMapper;
import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.model.ToolSetData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@LogExecution
@Slf4j
public class CoreDeploymentService {

    private final DeploymentClient deploymentClient;
    private final DeploymentClientMapper deploymentClientMapper;

    public Map<String, Object> getConfiguration(String deploymentName) {
        return deploymentClient.getConfiguration(deploymentName);
    }

    public Optional<ToolSetData> tryGetToolSet(String name) {
        try {
            return Optional.of(getToolSet(name));
        } catch (Exception e) {
            log.warn("Failed to retrieve '{}' toolset from DIAL Core: {}", name, e);
            return Optional.empty();
        }
    }

    private ToolSetData getToolSet(String name) {
        ToolSetDataDto toolSetDataDto = deploymentClient.getToolSet(name);
        return deploymentClientMapper.toToolSetData(toolSetDataDto);
    }
}
