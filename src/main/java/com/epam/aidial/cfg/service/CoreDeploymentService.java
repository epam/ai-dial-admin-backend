package com.epam.aidial.cfg.service;

import com.epam.aidial.cfg.client.DeploymentClient;
import com.epam.aidial.cfg.client.dto.ToolSetDataDto;
import com.epam.aidial.cfg.client.mapper.DeploymentClientMapper;
import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.model.ApplicationData;
import com.epam.aidial.cfg.model.DeploymentData;
import com.epam.aidial.cfg.model.DeploymentType;
import com.epam.aidial.cfg.model.InterfaceType;
import com.epam.aidial.cfg.model.ModelData;
import com.epam.aidial.cfg.model.ToolSetData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.List;
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

    public List<DeploymentData> listDeployments(List<InterfaceType> interfaceTypes,
                                                List<DeploymentType> types) {

        List<String> interfaceParams = InterfaceType.toParamValues(interfaceTypes);
        var deployments = deploymentClient.listDeployments(interfaceParams);

        return deploymentClientMapper.toDeploymentDataList(deployments).stream()
                .filter(deployment -> {
                    if (deployment == null) {
                        log.warn("Skipping deployment with unknown object type");
                        return false;
                    }
                    return CollectionUtils.isEmpty(types)
                            || types.stream().anyMatch(type -> matchesType(deployment, type));
                })
                .toList();
    }

    private static boolean matchesType(DeploymentData deployment, DeploymentType type) {
        return switch (type) {
            case MODEL -> deployment instanceof ModelData;
            case APPLICATION -> deployment instanceof ApplicationData;
            case TOOLSET -> deployment instanceof ToolSetData;
        };
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