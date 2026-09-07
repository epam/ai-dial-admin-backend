package com.epam.aidial.cfg.web.facade.mapper;

import com.epam.aidial.cfg.domain.model.DeploymentInterface;
import com.epam.aidial.cfg.domain.model.InterfaceMode;
import com.epam.aidial.cfg.dto.DeploymentInterfaceDto;
import com.epam.aidial.cfg.dto.InterfaceModeDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DeploymentInterfaceDtoMapper {

    DeploymentInterface toDomain(DeploymentInterfaceDto dto);

    DeploymentInterfaceDto toDto(DeploymentInterface domain);

    InterfaceMode map(InterfaceModeDto mode);

    InterfaceModeDto map(InterfaceMode mode);
}
