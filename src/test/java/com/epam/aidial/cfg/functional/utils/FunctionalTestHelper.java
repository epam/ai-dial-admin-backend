package com.epam.aidial.cfg.functional.utils;

import com.epam.aidial.cfg.dto.AdapterDto;
import com.epam.aidial.cfg.dto.AddonDto;
import com.epam.aidial.cfg.dto.ApplicationDto;
import com.epam.aidial.cfg.dto.AssistantDto;
import com.epam.aidial.cfg.dto.InterceptorDto;
import com.epam.aidial.cfg.dto.KeyDto;
import com.epam.aidial.cfg.dto.LimitDto;
import com.epam.aidial.cfg.dto.ModelDto;
import com.epam.aidial.cfg.dto.RoleDto;
import com.epam.aidial.cfg.dto.ToolSetDto;
import com.epam.aidial.cfg.dto.route.RouteDto;
import com.epam.aidial.cfg.dto.source.InterceptorEndpointsSourceDto;
import com.epam.aidial.cfg.dto.source.ModelEndpointsSourceDto;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public class FunctionalTestHelper {
    public static RoleDto createRoleDto(String suffix) {
        RoleDto roleDto = new RoleDto();
        roleDto.setName("role" + suffix);
        roleDto.setDescription("role" + suffix);
        roleDto.setDisplayName("role" + suffix);
        return roleDto;
    }

    public static ModelDto createModelDto(String suffix) {
        ModelDto modelDto = new ModelDto();
        modelDto.setName("model" + suffix);
        modelDto.setDisplayName("model" + suffix);
        modelDto.setDescription("description" + suffix);
        modelDto.setMaxRetryAttempts(1);
        return modelDto;
    }

    public static ModelDto createModelDtoWithEndpoint(String suffix) {
        ModelDto modelDto = createModelDto(suffix);
        modelDto.setEndpoint("https://endpoint1/chat/completions");
        return modelDto;
    }

    public static ModelDto createModelDtoWithLimitsAndEndpoint(String suffix) {
        ModelDto modelDto = createModelDtoWithEndpoint(suffix);
        modelDto.setRoleLimits(Map.of(
                "role" + suffix, new LimitDto()
        ));
        modelDto.setSource(new ModelEndpointsSourceDto());
        return modelDto;
    }


    public static AdapterDto createAdapterDto(String suffix) {
        AdapterDto adapterDto = new AdapterDto();
        adapterDto.setName("adapter" + suffix);
        adapterDto.setDisplayName("adapter" + suffix);
        adapterDto.setBaseEndpoint("https://endpoint.test.com/adapter" + suffix);
        adapterDto.setDescription("description" + suffix);
        adapterDto.setModels(List.of());
        return adapterDto;
    }

    public static ToolSetDto createToolSetDto(String suffix) {
        ToolSetDto toolSet = new ToolSetDto();
        toolSet.setName("ToolSet" + suffix);
        toolSet.setDisplayName("ToolSet" + suffix);
        toolSet.setDescription("description" + suffix);
        toolSet.setRoleLimits(Map.of(
                "role" + suffix, new LimitDto()
        ));
        toolSet.setMaxRetryAttempts(1);
        return toolSet;
    }

    public static RouteDto createRouteDto(String suffix) {
        RouteDto routeDto = new RouteDto();
        routeDto.setName("route" + suffix);
        routeDto.setDescription("description" + suffix);
        routeDto.setDisplayName("displayName" + suffix);
        return routeDto;
    }

    public static RouteDto createRouteDtoWithLimits(String suffix) {
        RouteDto routeDto = createRouteDto(suffix);
        routeDto.setRoleLimits(Map.of(
                "role" + suffix, new LimitDto()
        ));
        return routeDto;
    }

    public static AddonDto createAddonDto(String suffix) {
        AddonDto addonDto = new AddonDto();
        addonDto.setName("addon" + suffix);
        addonDto.setDisplayName("addon" + suffix);
        addonDto.setDescription("description" + suffix);
        return addonDto;
    }

    public static AddonDto createAddonWithRoleLimitsDto(String suffix) {
        AddonDto addonDto = createAddonDto(suffix);
        addonDto.setRoleLimits(Map.of(
                "role" + suffix, new LimitDto()));
        return addonDto;
    }

    public static AddonDto createAddonDtoWithAllLimits(String suffix) {
        return createAddonWithRoleLimitsDto(suffix);
    }

    public static KeyDto createKeyDto(String suffix) {
        KeyDto keyDto = new KeyDto();
        keyDto.setName("key" + suffix);
        keyDto.setKey("keyValue" + suffix);
        keyDto.setDescription("key" + suffix);
        keyDto.setDisplayName("key" + suffix);
        keyDto.setProject("project" + suffix);
        keyDto.setProjectContactPoint("test@mail.com");
        keyDto.setExpiresAt(Instant.ofEpochMilli(253402300799999L));
        return keyDto;
    }

    public static KeyDto createKeyDtoWithRole(String suffix) {
        KeyDto keyDto = createKeyDto(suffix);
        keyDto.setRoles(List.of("role" + suffix));
        return keyDto;
    }

    public static InterceptorDto createInterceptorDto(String suffix) {
        InterceptorDto interceptorDto = new InterceptorDto();
        interceptorDto.setName("interceptor" + suffix);
        interceptorDto.setDescription("description" + suffix);
        interceptorDto.setDisplayName("displayName" + suffix);
        interceptorDto.setEndpoint("https://endpoint.test.com/interceptor" + suffix);
        interceptorDto.setEntities(List.of());
        interceptorDto.setSource(new InterceptorEndpointsSourceDto());
        return interceptorDto;
    }

    public static InterceptorDto createInterceptorDtoWithEntities(String suffix) {
        InterceptorDto interceptorDto = createInterceptorDto(suffix);
        interceptorDto.setEntities(List.of("application" + suffix));
        return interceptorDto;
    }

    public static ApplicationDto createBaseApplicationDto(String suffix) {
        ApplicationDto applicationDto = new ApplicationDto();
        applicationDto.setName("application" + suffix);
        applicationDto.setDisplayName("application" + suffix);
        applicationDto.setDescription("description" + suffix);
        return applicationDto;
    }

    public static ApplicationDto createApplicationDtoWithEndpoint(String suffix) {
        ApplicationDto applicationDto = createBaseApplicationDto(suffix);
        applicationDto.setEndpoint("endpoint" + suffix);
        return applicationDto;
    }

    public static ApplicationDto createApplicationDtoWithEndpointAndLimits(String suffix) {
        ApplicationDto applicationDto = createApplicationDtoWithEndpoint(suffix);
        applicationDto.setRoleLimits(Map.of(
                "role" + suffix, new LimitDto()
        ));
        return applicationDto;
    }

    public static AssistantDto createAssistantDto(String suffix) {
        AssistantDto assistantDto = new AssistantDto();
        assistantDto.setName("assistant" + suffix);
        assistantDto.setDisplayName("assistant" + suffix);
        assistantDto.setDescription("description" + suffix);
        assistantDto.setRoleLimits(Map.of(
                "role" + suffix, new LimitDto()
        ));
        return assistantDto;
    }
}
