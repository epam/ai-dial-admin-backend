package com.epam.aidial.cfg.domain.mapper;

import com.epam.aidial.cfg.domain.model.DeploymentInterface;
import com.epam.aidial.core.config.CoreDeploymentInterface;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DeploymentInterfaceCoreMapper {

    CoreDeploymentInterface map(DeploymentInterface deploymentInterface);

    DeploymentInterface map(CoreDeploymentInterface coreDeploymentInterface);
}
