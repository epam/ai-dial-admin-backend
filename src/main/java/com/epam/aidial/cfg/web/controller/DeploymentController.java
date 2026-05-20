package com.epam.aidial.cfg.web.controller;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.dto.DeploymentDataDto;
import com.epam.aidial.cfg.mapper.DeploymentMapper;
import com.epam.aidial.cfg.model.DeploymentType;
import com.epam.aidial.cfg.model.InterfaceType;
import com.epam.aidial.cfg.service.CoreDeploymentService;
import com.epam.aidial.cfg.web.facade.DeploymentFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

import static org.springframework.web.bind.annotation.RequestMethod.HEAD;

@RestController
@RequestMapping("/api/v1/deployments")
@LogExecution
@RequiredArgsConstructor
public class DeploymentController {

    private final DeploymentFacade deploymentFacade;
    private final CoreDeploymentService coreDeploymentService;
    private final DeploymentMapper deploymentMapper;

    @RequestMapping(method = HEAD, path = "/{deploymentName}")
    public void ensureExists(@PathVariable String deploymentName) {
        deploymentFacade.ensureExists(deploymentName);
    }

    @GetMapping
    public List<DeploymentDataDto> listDeployments(
            @RequestParam(name = "interface_types", required = false) List<InterfaceType> interfaceTypes,
            @RequestParam(name = "deployment_types", required = false) List<DeploymentType> types) {
        var deployments = coreDeploymentService.listDeployments(interfaceTypes, types);
        return deploymentMapper.toDeploymentDataDtoList(deployments);
    }

    @GetMapping(path = "/{deploymentName}/configuration")
    public Map<String, Object> getConfiguration(@PathVariable String deploymentName) {
        return coreDeploymentService.getConfiguration(deploymentName);
    }

}