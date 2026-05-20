package com.epam.aidial.cfg.mapper;

import com.epam.aidial.cfg.dto.ApplicationDataDto;
import com.epam.aidial.cfg.dto.DeploymentDataDto;
import com.epam.aidial.cfg.dto.ModelDataDto;
import com.epam.aidial.cfg.dto.ToolSetDataDto;
import com.epam.aidial.cfg.model.ApplicationData;
import com.epam.aidial.cfg.model.DeploymentData;
import com.epam.aidial.cfg.model.ModelData;
import com.epam.aidial.cfg.model.ToolSetData;
import org.mapstruct.Mapper;
import org.mapstruct.SubclassMapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface DeploymentMapper {

    List<DeploymentDataDto> toDeploymentDataDtoList(List<DeploymentData> models);

    @SubclassMapping(source = ModelData.class, target = ModelDataDto.class)
    @SubclassMapping(source = ApplicationData.class, target = ApplicationDataDto.class)
    @SubclassMapping(source = ToolSetData.class, target = ToolSetDataDto.class)
    DeploymentDataDto toDeploymentDataDto(DeploymentData model);

    ModelDataDto toModelDataDto(ModelData model);

    ApplicationDataDto toApplicationDataDto(ApplicationData model);

    ToolSetDataDto toToolSetDataDto(ToolSetData model);
}