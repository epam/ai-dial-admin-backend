package com.epam.aidial.cfg.web.facade;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.domain.model.ToolSet;
import com.epam.aidial.cfg.domain.service.ToolSetService;
import com.epam.aidial.cfg.dto.CoreWithDomainHash;
import com.epam.aidial.cfg.dto.DtoWithDomainHash;
import com.epam.aidial.cfg.dto.EntitySyncStateDto;
import com.epam.aidial.cfg.dto.ResourceSignInRequestDto;
import com.epam.aidial.cfg.dto.ResourceSignOutRequestDto;
import com.epam.aidial.cfg.dto.ToolSetDto;
import com.epam.aidial.cfg.mapper.ResourceCredentialMapper;
import com.epam.aidial.cfg.model.ResourceAuthSettings;
import com.epam.aidial.cfg.model.ToolSetData;
import com.epam.aidial.cfg.service.CoreDeploymentService;
import com.epam.aidial.cfg.service.ResourceCredentialService;
import com.epam.aidial.cfg.service.core.CoreToolSetService;
import com.epam.aidial.cfg.web.facade.mapper.EntitySyncStateDtoMapper;
import com.epam.aidial.cfg.web.facade.mapper.ToolSetDtoMapper;
import com.epam.aidial.core.config.CoreToolSet;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@LogExecution
@Transactional
@RequiredArgsConstructor
public class ToolSetFacade {

    private final ToolSetService toolSetService;
    private final ToolSetDtoMapper mapper;
    private final CoreToolSetService coreToolSetService;
    private final EntitySyncStateDtoMapper entitySyncStateDtoMapper;
    private final ResourceCredentialService resourceCredentialService;
    private final ResourceCredentialMapper resourceCredentialMapper;
    private final CoreDeploymentService coreDeploymentService;

    public Collection<ToolSetDto> getAllToolSets() {
        return toolSetService.getAll()
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    public ToolSetDto getToolSet(String toolSetName) {
        ToolSet toolSet = toolSetService.get(toolSetName);
        return mapper.toDto(toolSet);
    }

    public DtoWithDomainHash<ToolSetDto> getToolSetWithHash(String toolSetName) {
        var modelWithHash = toolSetService.getToolSetWithHash(toolSetName);
        var toolSet = modelWithHash.model();
        var resourceAuthSettings = getResourceAuthSettings(toolSetName);
        var dto = mapper.toDto(toolSet, resourceAuthSettings);
        return new DtoWithDomainHash<>(dto, modelWithHash.hash());
    }

    public CoreWithDomainHash<CoreToolSet> getCoreToolSetWithHash(String toolSetName) {
        return coreToolSetService.getCoreToolSetWithHash(toolSetName);
    }

    public EntitySyncStateDto getSyncState(String toolSetName, String hash) {
        var syncState = coreToolSetService.getSyncState(toolSetName, hash);
        return entitySyncStateDtoMapper.toDto(syncState);
    }

    public void createToolSet(ToolSetDto toolSetDto) {
        Optional.of(toolSetDto)
                .map(mapper::toDomain)
                .ifPresent(toolSetService::create);
    }

    public String updateToolSet(String toolSetName, ToolSetDto toolSetDto, String hash) {
        ToolSet value = mapper.toDomain(toolSetDto);
        return toolSetService.update(toolSetName, value, hash);
    }

    public String updateToolSet(String toolSetName, CoreToolSet coreToolSet, String toolSet) {
        return coreToolSetService.updateToolSet(toolSetName, coreToolSet, toolSet);
    }

    public void deleteToolSet(String toolSetName) {
        toolSetService.delete(toolSetName);
    }

    public ToolSetDto getSnapshot(String toolSetName, Integer revision) {
        ToolSet toolSet = toolSetService.getSnapshot(toolSetName, revision);
        return mapper.toDto(toolSet);
    }

    public Collection<ToolSetDto> getAllAtRevision(Integer revision) {
        return toolSetService.getAllAtRevision(revision)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    public McpSchema.ListToolsResult getDiscoveredTools(String toolSetName, String nextCursor) {
        return toolSetService.getDiscoveredTools(toolSetName, nextCursor);
    }

    public McpSchema.CallToolResult callTool(String toolSetName, McpSchema.CallToolRequest callToolRequest) {
        return toolSetService.callTool(toolSetName, callToolRequest);
    }

    public void refreshEndpoints() {
        toolSetService.refreshEndpoints();
    }

    public void signIn(@RequestBody ResourceSignInRequestDto requestDto) {
        resourceCredentialService.signInToolSet(resourceCredentialMapper.toResourceSignInRequest(requestDto));
    }

    public void signOut(@RequestBody ResourceSignOutRequestDto requestDto) {
        resourceCredentialService.signOutToolSet(resourceCredentialMapper.toResourceSignOutRequest(requestDto));
    }

    private ResourceAuthSettings getResourceAuthSettings(String toolSetName) {
        return coreDeploymentService.tryGetToolSet(toolSetName)
                .map(ToolSetData::getAuthSettings)
                .orElse(null);
    }
}