package com.epam.aidial.cfg.web.facade;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.domain.model.ToolSet;
import com.epam.aidial.cfg.domain.service.ToolSetService;
import com.epam.aidial.cfg.dto.ShareResourceLimitDto;
import com.epam.aidial.cfg.dto.ToolSetDto;
import com.epam.aidial.cfg.web.facade.mapper.ToolSetDtoMapper;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public void createToolSet(ToolSetDto toolSetDto) {
        setDefaultRoleShareResourceLimitIfMissing(toolSetDto);
        Optional.of(toolSetDto)
                .map(mapper::toDomain)
                .ifPresent(toolSetService::create);
    }

    public void updateToolSet(String toolSetName, ToolSetDto toolSetDto) {
        setDefaultRoleShareResourceLimitIfMissing(toolSetDto);
        ToolSet value = mapper.toDomain(toolSetDto);
        toolSetService.update(toolSetName, value);
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

    private void setDefaultRoleShareResourceLimitIfMissing(ToolSetDto toolSetDto) {
        ShareResourceLimitDto defaultRoleShareResourceLimit = toolSetDto.getDefaultRoleShareResourceLimit();
        if (defaultRoleShareResourceLimit == null) {
            defaultRoleShareResourceLimit = new ShareResourceLimitDto();
            toolSetDto.setDefaultRoleShareResourceLimit(defaultRoleShareResourceLimit);
        }
    }
}
