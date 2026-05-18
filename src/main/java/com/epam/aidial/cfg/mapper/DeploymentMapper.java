package com.epam.aidial.cfg.mapper;

import com.epam.aidial.cfg.dto.ApplicationDataDto;
import com.epam.aidial.cfg.dto.AuthenticationTypeDto;
import com.epam.aidial.cfg.dto.DeploymentDataDto;
import com.epam.aidial.cfg.dto.FeaturesDataDto;
import com.epam.aidial.cfg.dto.ModelCapabilitiesDataDto;
import com.epam.aidial.cfg.dto.ModelDataDto;
import com.epam.aidial.cfg.dto.ModelLimitsDataDto;
import com.epam.aidial.cfg.dto.ModelPricingDataDto;
import com.epam.aidial.cfg.dto.ResourceAuthSettingsDto;
import com.epam.aidial.cfg.dto.RouteResourceDto;
import com.epam.aidial.cfg.dto.ScaleSettingsDataDto;
import com.epam.aidial.cfg.dto.ToolSetDataDto;
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
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface DeploymentMapper {

    default List<DeploymentDataDto> toDeploymentDataDtoList(List<DeploymentData> models) {
        if (models == null) {
            return List.of();
        }
        return models.stream().map(this::toDeploymentDataDto).toList();
    }

    default DeploymentDataDto toDeploymentDataDto(DeploymentData model) {
        if (model == null) {
            return null;
        }
        if (model instanceof ModelData modelData) {
            return toModelDataDto(modelData);
        }
        if (model instanceof ApplicationData applicationData) {
            return toApplicationDataDto(applicationData);
        }
        if (model instanceof ToolSetData toolSetData) {
            return toToolSetDataDto(toolSetData);
        }
        return toBaseDeploymentDataDto(model);
    }

    ModelDataDto toModelDataDto(ModelData model);

    @Mapping(target = "routes", expression = "java(mapRoutes(model.getRoutes()))")
    ApplicationDataDto toApplicationDataDto(ApplicationData model);

    ToolSetDataDto toToolSetDataDto(ToolSetData model);

    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "updatedAt", source = "updatedAt")
    DeploymentDataDto toBaseDeploymentDataDto(DeploymentData model);

    ScaleSettingsDataDto toScaleSettingsDataDto(ScaleSettingsData model);

    FeaturesDataDto toFeaturesDataDto(FeaturesData model);

    ModelCapabilitiesDataDto toModelCapabilitiesDataDto(ModelCapabilitiesData model);

    ModelLimitsDataDto toModelLimitsDataDto(ModelLimitsData model);

    ModelPricingDataDto toModelPricingDataDto(ModelPricingData model);

    ResourceAuthSettingsDto toResourceAuthSettingsDto(ResourceAuthSettings model);

    default AuthenticationTypeDto toAuthenticationTypeDto(AuthenticationType type) {
        return type == null ? null : AuthenticationTypeDto.valueOf(type.name());
    }

    default ResourceAuthSettingsDto.ResourceAuthStatus mapResourceAuthStatusDto(
            ResourceAuthSettings.ResourceAuthStatus status) {
        return status == null ? null
                : ResourceAuthSettingsDto.ResourceAuthStatus.valueOf(status.name());
    }

    RouteResourceDto toRouteResourceDto(RouteResource route);

    default Map<String, RouteResourceDto> mapRoutes(Map<String, RouteResource> routes) {
        if (routes == null) {
            return null;
        }
        return routes.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> toRouteResourceDto(entry.getValue()),
                        (left, right) -> left,
                        LinkedHashMap::new));
    }

    default Long map(long value) {
        return value == 0 ? null : value;
    }

    default long map(Long value) {
        return value == null ? 0L : value;
    }
}