package com.epam.aidial.cfg.client.mapper;

import com.epam.aidial.cfg.client.dto.ApplicationDataDto;
import com.epam.aidial.cfg.client.dto.DeploymentDataDto;
import com.epam.aidial.cfg.client.dto.ModelDataDto;
import com.epam.aidial.cfg.client.dto.ToolSetDataDto;
import com.epam.aidial.cfg.model.ApplicationData;
import com.epam.aidial.cfg.model.DeploymentData;
import com.epam.aidial.cfg.model.ModelData;
import com.epam.aidial.cfg.model.ToolSetData;
import org.mapstruct.Mapper;
import org.mapstruct.SubclassMapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = RouteMapper.class)
public interface DeploymentClientMapper {

    List<DeploymentData> toDeploymentDataList(List<DeploymentDataDto> dtos);

    @SubclassMapping(source = ModelDataDto.class, target = ModelData.class)
    @SubclassMapping(source = ApplicationDataDto.class, target = ApplicationData.class)
    @SubclassMapping(source = ToolSetDataDto.class, target = ToolSetData.class)
    DeploymentData toDeploymentData(DeploymentDataDto dto);

    ApplicationData toApplicationData(ApplicationDataDto dto);

    ModelData toModelData(ModelDataDto dto);

    ToolSetData toToolSetData(ToolSetDataDto dto);
}