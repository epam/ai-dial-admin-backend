package com.epam.aidial.cfg.web.facade;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.domain.service.DeploymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@LogExecution
public class DeploymentFacade {

    private final DeploymentService deploymentService;

    public void ensureExists(String deploymentName) {
        deploymentService.ensureExists(deploymentName);
    }
}