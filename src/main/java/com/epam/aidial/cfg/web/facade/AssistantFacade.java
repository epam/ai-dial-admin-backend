package com.epam.aidial.cfg.web.facade;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.domain.model.Assistant;
import com.epam.aidial.cfg.domain.service.AssistantService;
import com.epam.aidial.cfg.dto.AssistantDto;
import com.epam.aidial.cfg.web.facade.mapper.AssistantDtoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@LogExecution
public class AssistantFacade {

    private final AssistantService assistantService;
    private final AssistantDtoMapper mapper;

    public Collection<AssistantDto> getAllAssistants() {
        return assistantService.getAllAssistants()
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    public AssistantDto getAssistant(String assistantName) {
        Assistant assistant = assistantService.getAssistant(assistantName);
        return mapper.toDto(assistant);
    }

    public void createAssistant(AssistantDto assistantDto) {
        Optional.of(assistantDto)
                .map(mapper::toDomain)
                .ifPresent(assistantService::createAssistant);
    }

    public void updateAssistant(String assistantName, AssistantDto assistantDto) {
        Assistant value = mapper.toDomain(assistantDto);
        assistantService.updateAssistant(assistantName, value);
    }

    public void deleteAssistant(String assistantName) {
        assistantService.deleteAssistant(assistantName);
    }

    public AssistantDto getSnapshot(String assistantName, Integer revision) {
        Assistant assistant = assistantService.getSnapshot(assistantName, revision);
        return mapper.toDto(assistant);
    }

    public Collection<AssistantDto> getAllAtRevision(Integer revision) {
        return assistantService.getAllAtRevision(revision)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }
}
