package com.epam.aidial.cfg.client.mapper;

import com.epam.aidial.cfg.client.dto.ApplicationDataDto;
import com.epam.aidial.cfg.client.dto.DeploymentDataDto;
import com.epam.aidial.cfg.client.dto.ModelDataDto;
import com.epam.aidial.cfg.client.dto.ToolSetDataDto;
import com.epam.aidial.cfg.model.ApplicationData;
import com.epam.aidial.cfg.model.DeploymentData;
import com.epam.aidial.cfg.model.ModelData;
import com.epam.aidial.cfg.model.RouteResource;
import com.epam.aidial.cfg.model.ToolSetData;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.AfterMapping;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.SubclassMapping;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Mapper(
        componentModel = "spring",
        builder = @Builder(disableBuilder = true)
)
@Slf4j
public abstract class DeploymentClientMapper {

    @Autowired
    protected RouteMapper routeMapper;

    public List<DeploymentData> toDeploymentDataList(List<DeploymentDataDto> dtos) {
        if (dtos == null) {
            return List.of();
        }

        return dtos.stream()
                .map(this::toDeploymentData)
                .filter(Objects::nonNull)
                .toList();
    }

    @SubclassMapping(source = ModelDataDto.class, target = ModelData.class)
    @SubclassMapping(source = ApplicationDataDto.class, target = ApplicationData.class)
    @SubclassMapping(source = ToolSetDataDto.class, target = ToolSetData.class)
    public abstract DeploymentData toDeploymentData(DeploymentDataDto dto);

    @Mapping(target = "routes", ignore = true)
    public abstract ApplicationData toApplicationData(ApplicationDataDto dto);

    @AfterMapping
    protected void fillRoutes(
            ApplicationDataDto dto,
            @MappingTarget ApplicationData target
    ) {
        if (dto.getRoutes() != null) {
            List<RouteResource> routes = routeMapper.toRouteList(dto.getRoutes());

            target.setRoutes(routes.stream()
                    .collect(Collectors.toMap(
                            RouteResource::getName,
                            route -> route,
                            (left, right) -> left,
                            LinkedHashMap::new
                    )));
        }
    }

    public abstract ModelData toModelData(ModelDataDto dto);

    public abstract ToolSetData toToolSetData(ToolSetDataDto dto);
}