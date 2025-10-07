package com.epam.aidial.cfg.web.facade;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.domain.model.Application;
import com.epam.aidial.cfg.domain.service.ApplicationService;
import com.epam.aidial.cfg.dto.ApplicationDto;
import com.epam.aidial.cfg.dto.ApplicationInfoDto;
import com.epam.aidial.cfg.web.facade.mapper.ApplicationDtoMapper;
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

    public void createApplication(ApplicationDto applicationDto) {
        Optional.of(applicationDto)
                .map(mapper::toDomain)
                .ifPresent(applicationService::createApplication);
    }

    public void updateApplication(String applicationName, ApplicationDto applicationDto) {
        Application value = mapper.toDomain(applicationDto);
        applicationService.updateApplication(applicationName, value);
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