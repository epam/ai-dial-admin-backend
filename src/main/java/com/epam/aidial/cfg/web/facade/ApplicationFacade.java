package com.epam.aidial.cfg.web.facade;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.domain.model.Application;
import com.epam.aidial.cfg.domain.service.ApplicationService;
import com.epam.aidial.cfg.dto.ApplicationDto;
import com.epam.aidial.cfg.dto.ApplicationInfoDto;
import com.epam.aidial.cfg.dto.CoreWithDomainHash;
import com.epam.aidial.cfg.dto.DtoWithDomainHash;
import com.epam.aidial.cfg.dto.EntitySyncStateDto;
import com.epam.aidial.cfg.service.core.CoreApplicationService;
import com.epam.aidial.cfg.web.facade.mapper.ApplicationDtoMapper;
import com.epam.aidial.cfg.web.facade.mapper.EntitySyncStateDtoMapper;
import com.epam.aidial.core.config.CoreApplication;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@LogExecution
public class ApplicationFacade {

    private final ApplicationService applicationService;
    private final ApplicationDtoMapper mapper;
    private final CoreApplicationService coreApplicationService;
    private final EntitySyncStateDtoMapper entitySyncStateDtoMapper;

    public Collection<ApplicationInfoDto> getAllApplications() {
        return applicationService.getAllApplications()
                .stream()
                .map(mapper::toApplicationInfoDto)
                .toList();
    }

    public ApplicationDto getApplication(String applicationName) {
        Application application = applicationService.getApplication(applicationName);
        return mapper.toDto(application);
    }

    public DtoWithDomainHash<ApplicationDto> getApplicationWithHash(String applicationName) {
        var applicationWithHash = applicationService.getApplicationWithHash(applicationName);
        return new DtoWithDomainHash<>(mapper.toDto(applicationWithHash.model()), applicationWithHash.hash());
    }

    public CoreWithDomainHash<CoreApplication> getCoreApplicationWithHash(String applicationName) {
        return coreApplicationService.getCoreApplicationWithHash(applicationName);
    }

    public EntitySyncStateDto getSyncState(String applicationName, String hash) {
        var syncState = coreApplicationService.getSyncState(applicationName, hash);
        return entitySyncStateDtoMapper.toDto(syncState);
    }

    public void createApplication(ApplicationDto applicationDto) {
        Optional.of(applicationDto)
                .map(mapper::toDomain)
                .ifPresent(applicationService::createApplication);
    }

    public String updateApplication(String applicationName, ApplicationDto applicationDto, String hash) {
        Application value = mapper.toDomain(applicationDto);
        return applicationService.updateApplication(applicationName, value, hash);
    }

    public String updateApplication(String applicationName, CoreApplication coreApplication, String hash) {
        return coreApplicationService.updateApplication(applicationName, coreApplication, hash);
    }

    public void deleteApplication(String applicationName) {
        applicationService.deleteApplication(applicationName);
    }

    public ApplicationDto getSnapshot(String applicationName, Integer revision) {
        Application application = applicationService.getSnapshot(applicationName, revision);
        return mapper.toDto(application);
    }

    public Collection<ApplicationDto> getAllAtRevision(Integer revision) {
        return applicationService.getAllAtRevision(revision)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }
}