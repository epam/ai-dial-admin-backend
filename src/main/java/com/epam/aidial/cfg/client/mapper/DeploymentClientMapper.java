package com.epam.aidial.cfg.client.mapper;

import com.epam.aidial.cfg.client.dto.ApplicationDataDto;
import com.epam.aidial.cfg.client.dto.DeploymentDataDto;
import com.epam.aidial.cfg.client.dto.ModelDataDto;
import com.epam.aidial.cfg.client.dto.ToolSetDataDto;
import com.epam.aidial.cfg.model.ApplicationData;
import com.epam.aidial.cfg.model.DeploymentData;
import com.epam.aidial.cfg.model.ModelData;
import com.epam.aidial.cfg.model.ToolSetData;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.Mapper;
import org.mapstruct.SubclassMapping;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Mapper(componentModel = "spring")
@Slf4j
public abstract class DeploymentClientMapper {

    @Autowired
    protected RouteMapper routeMapper;

    public abstract List<DeploymentData> toDeploymentDataList(List<DeploymentDataDto> dtos);

    @SubclassMapping(source = ModelDataDto.class, target = ModelData.class)
    @SubclassMapping(source = ApplicationDataDto.class, target = ApplicationData.class)
    @SubclassMapping(source = ToolSetDataDto.class, target = ToolSetData.class)
    public abstract DeploymentData toDeploymentData(DeploymentDataDto dto);

    public abstract ApplicationData toApplicationData(ApplicationDataDto dto);

    public abstract ModelData toModelData(ModelDataDto dto);

    public abstract ToolSetData toToolSetData(ToolSetDataDto dto);
}