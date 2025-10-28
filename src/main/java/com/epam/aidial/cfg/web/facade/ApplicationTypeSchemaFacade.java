package com.epam.aidial.cfg.web.facade;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.domain.model.ApplicationTypeSchema;
import com.epam.aidial.cfg.domain.service.ApplicationTypeSchemaService;
import com.epam.aidial.cfg.dto.ApplicationTypeSchemaDto;
import com.epam.aidial.cfg.dto.CoreWithDomainHash;
import com.epam.aidial.cfg.dto.DtoWithDomainHash;
import com.epam.aidial.cfg.service.core.CoreApplicationTypeSchemaService;
import com.epam.aidial.cfg.web.facade.mapper.ApplicationTypeSchemaDtoMapper;
import com.epam.aidial.core.config.CoreApplicationTypeSchema;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@LogExecution
public class ApplicationTypeSchemaFacade {

    private final ApplicationTypeSchemaService schemaService;
    private final ApplicationTypeSchemaDtoMapper mapper;
    private final CoreApplicationTypeSchemaService coreApplicationTypeSchemaService;

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

    public DtoWithDomainHash<ApplicationTypeSchemaDto> getSchemaWithHash(String id) {
        var schemaWithHash = schemaService.getSchemaWithHash(id);
        return new DtoWithDomainHash<>(mapper.toDto(schemaWithHash.model()), schemaWithHash.hash());
    }

    public void create(@Valid ApplicationTypeSchemaDto schemaDto) {
        Optional.of(schemaDto)
                .map(mapper::toDomain)
                .ifPresent(schemaService::create);
    }

    public String update(String id, @Valid ApplicationTypeSchemaDto dto, String hash) {
        ApplicationTypeSchema value = mapper.toDomain(dto);
        return schemaService.update(id, value, hash);
    }

    public void delete(String schemaId, boolean removeApplication) {
        schemaService.delete(schemaId, removeApplication);
    }

    public ApplicationTypeSchemaDto getSnapshot(String id, Integer revision) {
        ApplicationTypeSchema applicationTypeSchema = schemaService.getSnapshot(id, revision);
        return mapper.toDto(applicationTypeSchema);
    }

    public Collection<ApplicationTypeSchemaDto> getAllAtRevision(Integer revision) {
        return schemaService.getAllAtRevision(revision)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    public CoreWithDomainHash<CoreApplicationTypeSchema> getCoreSchemaWithHash(String id) {
        return coreApplicationTypeSchemaService.getCoreSchemaWithHash(id);
    }

    public String update(String id, CoreApplicationTypeSchema coreApplicationTypeSchema, String hash) {
        return coreApplicationTypeSchemaService.updateSchema(id, coreApplicationTypeSchema, hash);
    }
}
