package com.epam.aidial.cfg.functional.utils;

import com.epam.aidial.cfg.dto.AdapterDto;
import com.epam.aidial.cfg.dto.AddonDto;
import com.epam.aidial.cfg.dto.AdminSettingsDto;
import com.epam.aidial.cfg.dto.ApplicationDto;
import com.epam.aidial.cfg.dto.AssistantDto;
import com.epam.aidial.cfg.dto.AuditActivityDto;
import com.epam.aidial.cfg.dto.InterceptorDto;
import com.epam.aidial.cfg.dto.InterceptorRunnerDto;
import com.epam.aidial.cfg.dto.KeyDto;
import com.epam.aidial.cfg.dto.LimitDto;
import com.epam.aidial.cfg.dto.McpDto;
import com.epam.aidial.cfg.dto.ModelDto;
import com.epam.aidial.cfg.dto.RoleDto;
import com.epam.aidial.cfg.dto.ToolSetDto;
import com.epam.aidial.cfg.dto.ValidityStateDto;
import com.epam.aidial.cfg.dto.route.RouteDto;
import com.epam.aidial.cfg.dto.source.AdapterEndpointsSourceDto;
import com.epam.aidial.cfg.dto.source.InterceptorEndpointsSourceDto;
import com.epam.aidial.cfg.dto.source.ModelAdapterSourceDto;
import com.epam.aidial.cfg.dto.source.ModelEndpointsSourceDto;
import com.epam.aidial.core.config.CoreFeatures;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class FunctionalTestHelper {
    public static RoleDto createRoleDto(String suffix) {
        RoleDto roleDto = new RoleDto();
        roleDto.setName("role" + suffix);
        roleDto.setDescription("role" + suffix);
        roleDto.setDisplayName("role" + suffix);
        roleDto.setTopics(new TreeSet<>(Set.of("role" + suffix)));
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

    public static ModelDto createModelDtoWithAdapter(String suffix) {
        ModelDto modelDto = createModelDto(suffix);
        modelDto.setSource(new ModelAdapterSourceDto("adapter" + suffix, "https://endpoint1/chat/completions"));
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
        adapterDto.setTopics(new TreeSet<>(Set.of("topic" + suffix)));
        adapterDto.setModels(List.of());
        adapterDto.setSource(new AdapterEndpointsSourceDto());
        return adapterDto;
    }

    public static ToolSetDto createToolSetDtoWithoutRoleLimits(String suffix) {
        ToolSetDto toolSet = new ToolSetDto();
        toolSet.setName("ToolSet" + suffix);
        toolSet.setDisplayName("ToolSet" + suffix);
        toolSet.setDescription("description" + suffix);
        toolSet.setEndpoint("https://endpoint.test.com/toolset" + suffix);
        toolSet.setTransport(ToolSetDto.TransportDto.HTTP);
        toolSet.setMaxRetryAttempts(1);
        return toolSet;
    }

    public static ToolSetDto createToolSetDto(String suffix) {
        ToolSetDto toolSet = createToolSetDtoWithoutRoleLimits(suffix);
        toolSet.setRoleLimits(Map.of(
                "role" + suffix, new LimitDto()
        ));
        return toolSet;
    }

    public static RouteDto createRouteDto(String suffix) {
        RouteDto routeDto = new RouteDto();
        routeDto.setTopics(new TreeSet<>(Set.of("topic" + suffix)));
        routeDto.setName("route" + suffix);
        routeDto.setDescription("description" + suffix);
        routeDto.setDisplayName("displayName" + suffix);
        routeDto.setPaths(List.of("path" + suffix));
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

    public static KeyDto createDto(String suffix, List<String> roles) {
        KeyDto keyDto = createKeyDto(suffix);
        keyDto.setRoles(roles);
        return keyDto;
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
        keyDto.setTopics(new TreeSet<>(Set.of("topic" + suffix)));
        keyDto.setAllowedIpAddressRanges(List.of("198.51.100.14/24", "2002::1234:abcd:ffff:c0a8:101/64"));
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
        interceptorDto.setTopics(new TreeSet<>(Set.of("topic1", "topic2")));
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

    public static ApplicationDto createApplicationDtoWithMcp(String suffix) {
        ApplicationDto applicationDto = createBaseApplicationDto(suffix);
        var mcp = new McpDto();
        mcp.setEndpoint("http://localhost:9876/mcp");
        mcp.setAllowedTools(List.of("classify_text"));
        applicationDto.setMcp(mcp);
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

    public static CoreFeatures defaultCoreFeatures() {
        CoreFeatures features = new CoreFeatures();

        features.setSystemPromptSupported(true);
        features.setToolsSupported(false);
        features.setSeedSupported(false);
        features.setUrlAttachmentsSupported(false);
        features.setFolderAttachmentsSupported(false);
        features.setAllowResume(true);
        features.setAccessibleByPerRequestKey(true);
        features.setContentPartsSupported(false);
        features.setTemperatureSupported(true);
        features.setParallelToolCallsSupported(true);
        features.setAssistantAttachmentsInRequestSupported(false);

        return features;
    }

    public static ValidityStateDto validState() {
        ValidityStateDto validityStateDto = new ValidityStateDto();
        validityStateDto.setValid(true);
        return validityStateDto;
    }

    public static ValidityStateDto invalidState(String message) {
        ValidityStateDto validityStateDto = new ValidityStateDto();
        validityStateDto.setMessage(message);
        validityStateDto.setValid(false);
        return validityStateDto;
    }

    public static AdminSettingsDto adminSettingsDto(String coreConfigVersion) {
        AdminSettingsDto adminSettingsDto = new AdminSettingsDto();
        adminSettingsDto.setCoreConfigVersion(coreConfigVersion);
        return adminSettingsDto;
    }

    public static InterceptorRunnerDto createInterceptorRunnerDto(String suffix) {
        InterceptorRunnerDto interceptorRunnerDto = new InterceptorRunnerDto();
        interceptorRunnerDto.setName("interceptorRunner" + suffix);
        interceptorRunnerDto.setDisplayName("Interceptor Runner " + suffix);
        interceptorRunnerDto.setDescription("description" + suffix);
        interceptorRunnerDto.setCompletionEndpoint("https://endpoint.test.com/completion" + suffix);
        interceptorRunnerDto.setConfigurationEndpoint("https://endpoint.test.com/configuration" + suffix);
        interceptorRunnerDto.setInterceptors(new ArrayList<>());
        return interceptorRunnerDto;
    }

    public static AuditActivityDto createAuditActivityDto(String activityType, String resourceType, String resourceId, Long epochTimestampMs) {
        AuditActivityDto auditActivityDto = new AuditActivityDto();

        auditActivityDto.setActivityType(activityType);
        auditActivityDto.setResourceType(resourceType);
        auditActivityDto.setResourceId(resourceId);
        auditActivityDto.setEpochTimestampMs(epochTimestampMs);

        return auditActivityDto;
    }

    public static AuditActivityDto createAuditActivityDto(String activityType, String resourceType, String resourceId) {
        AuditActivityDto auditActivityDto = new AuditActivityDto();

        auditActivityDto.setActivityType(activityType);
        auditActivityDto.setResourceType(resourceType);
        auditActivityDto.setResourceId(resourceId);

        return auditActivityDto;
    }
}