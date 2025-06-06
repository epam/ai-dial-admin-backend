package com.epam.aidial.cfg.web.facade;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.domain.model.ApplicationTypeSchema;
import com.epam.aidial.cfg.domain.service.ApplicationTypeSchemaService;
import com.epam.aidial.cfg.dto.ApplicationDto;
import com.epam.aidial.cfg.dto.ApplicationTypeSchemaDto;
import com.epam.aidial.cfg.web.facade.mapper.ApplicationTypeSchemaDtoMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Optional;

@RequiredArgsConstructor
@Service
@LogExecution
public class ApplicationTypeSchemaFacade {

    private final ApplicationTypeSchemaService schemaService;
    private final ApplicationTypeSchemaDtoMapper mapper;

    public Collection<ApplicationTypeSchemaDto> getAll() {
        return schemaService.getAll()
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    public ApplicationTypeSchemaDto get(String id) {
        ApplicationTypeSchema applicationTypeSchema = schemaService.get(id);
        return mapper.toDto(applicationTypeSchema);
    }

    public void create(@Valid ApplicationTypeSchemaDto schemaDto) {
        Optional.of(schemaDto)
                .map(mapper::toDomain)
                .ifPresent(schemaService::create);
    }

    public void update(String id, @Valid ApplicationTypeSchemaDto dto) {
        ApplicationTypeSchema value = mapper.toDomain(dto);
        schemaService.update(id, value);
    }

    public void delete(String schemaId, boolean removeApplication) {
        schemaService.delete(schemaId, removeApplication);
    }

    public ApplicationTypeSchemaDto getSnapshot(String id, Integer revision) {
        ApplicationTypeSchema applicationTypeSchema = schemaService.getSnapshot(id, revision);
        return mapper.toDto(applicationTypeSchema);
    }
}
