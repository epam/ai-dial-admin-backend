package com.epam.aidial.cfg.service;

import com.epam.aidial.cfg.client.OpenaiDeploymentsClient;
import com.epam.aidial.cfg.client.dto.ToolSetDataDto;
import com.epam.aidial.cfg.client.mapper.OpenaiDeploymentsClientMapper;
import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.model.ToolSetData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@LogExecution
@Slf4j
public class OpenaiDeploymentsService {

    private final OpenaiDeploymentsClient openaiDeploymentsClient;
    private final OpenaiDeploymentsClientMapper openaiDeploymentsClientMapper;

    public Optional<ToolSetData> tryGetToolSet(String name) {
        try {
            return Optional.of(getToolSet(name));
        } catch (Exception e) {
            log.info("Failed to retrieve '{}' toolset from DIAL Core: {}", name, e.getMessage());
            return Optional.empty();
        }
    }

    private ToolSetData getToolSet(String name) {
        ToolSetDataDto toolSetDataDto = openaiDeploymentsClient.getOpenaiToolSet(name);
        return openaiDeploymentsClientMapper.toToolSetData(toolSetDataDto);
    }
}