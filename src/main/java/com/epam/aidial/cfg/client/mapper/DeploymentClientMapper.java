package com.epam.aidial.cfg.client.mapper;

import com.epam.aidial.cfg.client.dto.ApplicationDataDto;
import com.epam.aidial.cfg.client.dto.AuthenticationTypeDto;
import com.epam.aidial.cfg.client.dto.DeploymentDataDto;
import com.epam.aidial.cfg.client.dto.FeaturesDataDto;
import com.epam.aidial.cfg.client.dto.ModelCapabilitiesDataDto;
import com.epam.aidial.cfg.client.dto.ModelDataDto;
import com.epam.aidial.cfg.client.dto.ModelLimitsDataDto;
import com.epam.aidial.cfg.client.dto.ModelPricingDataDto;
import com.epam.aidial.cfg.client.dto.ResourceAuthSettingsDto;
import com.epam.aidial.cfg.client.dto.ScaleSettingsDataDto;
import com.epam.aidial.cfg.client.dto.ToolSetDataDto;
import com.epam.aidial.cfg.model.ApplicationData;
import com.epam.aidial.cfg.model.AuthenticationType;
import com.epam.aidial.cfg.model.DeploymentData;
import com.epam.aidial.cfg.model.FeaturesData;
import com.epam.aidial.cfg.model.ModelCapabilitiesData;
import com.epam.aidial.cfg.model.ModelData;
import com.epam.aidial.cfg.model.ModelLimitsData;
import com.epam.aidial.cfg.model.ModelPricingData;
import com.epam.aidial.cfg.model.ResourceAuthSettings;
import com.epam.aidial.cfg.model.RouteResource;
import com.epam.aidial.cfg.model.ScaleSettingsData;
import com.epam.aidial.cfg.model.ToolSetData;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
@Slf4j
public abstract class DeploymentClientMapper {

    @Autowired
    private RouteMapper routeMapper;

    public List<DeploymentData> toDeploymentDataList(List<DeploymentDataDto> dtos) {
        if (dtos == null) {
            return List.of();
        }
        return dtos.stream()
                .map(this::toDeploymentData)
                .filter(Objects::nonNull)
                .toList();
    }

    public DeploymentData toDeploymentData(DeploymentDataDto dto) {
        if (dto == null) {
            return null;
        }
        if (dto instanceof ModelDataDto modelDataDto) {
            return toModelData(modelDataDto);
        }
        if (dto instanceof ApplicationDataDto applicationDataDto) {
            return toApplicationData(applicationDataDto);
        }
        if (dto instanceof ToolSetDataDto toolSetDataDto) {
            return toToolSetData(toolSetDataDto);
        }
        log.warn("Skipping deployment with unknown object type '{}': id='{}'", dto.getObject(), dto.getId());
        return null;
    }

    public ApplicationData toApplicationData(ApplicationDataDto dto) {
        if (dto == null) {
            return null;
        }
        ApplicationData applicationData = mapApplicationData(dto);
        if (dto.getRoutes() != null) {
            List<RouteResource> routes = routeMapper.toRouteList(dto.getRoutes());
            applicationData.setRoutes(routes.stream()
                    .collect(Collectors.toMap(
                            RouteResource::getName,
                            route -> route,
                            (left, right) -> left,
                            LinkedHashMap::new)));
        }
        return applicationData;
    }

    @Mapping(target = "routes", ignore = true)
    protected abstract ApplicationData mapApplicationData(ApplicationDataDto dto);

    public abstract ModelData toModelData(ModelDataDto dto);

    public abstract ToolSetData toToolSetData(ToolSetDataDto dto);

    protected abstract ScaleSettingsData toScaleSettingsData(ScaleSettingsDataDto dto);

    protected abstract FeaturesData toFeaturesData(FeaturesDataDto dto);

    protected abstract ModelCapabilitiesData toModelCapabilitiesData(ModelCapabilitiesDataDto dto);

    protected abstract ModelLimitsData toModelLimitsData(ModelLimitsDataDto dto);

    protected abstract ModelPricingData toModelPricingData(ModelPricingDataDto dto);

    protected abstract ResourceAuthSettings toResourceAuthSettings(ResourceAuthSettingsDto dto);

    protected AuthenticationType toAuthenticationType(AuthenticationTypeDto dto) {
        return dto == null ? null : AuthenticationType.valueOf(dto.name());
    }

    protected ResourceAuthSettings.ResourceAuthStatus toResourceAuthStatus(
            ResourceAuthSettingsDto.ResourceAuthStatus status) {
        return status == null ? null : ResourceAuthSettings.ResourceAuthStatus.valueOf(status.name());
    }
}