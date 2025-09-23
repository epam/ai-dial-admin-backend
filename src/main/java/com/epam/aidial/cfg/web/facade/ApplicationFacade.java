package com.epam.aidial.cfg.web.facade;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.domain.model.Application;
import com.epam.aidial.cfg.domain.service.ApplicationService;
import com.epam.aidial.cfg.dto.ApplicationDto;
import com.epam.aidial.cfg.dto.ApplicationInfoDto;
import com.epam.aidial.cfg.dto.DtoWithDomainHash;
import com.epam.aidial.cfg.dto.ShareResourceLimitDto;
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

    public DtoWithDomainHash<ApplicationDto> getApplicationWithHash(String applicationName) {
        var applicationWithHash = applicationService.getApplicationWithHash(applicationName);
        return new DtoWithDomainHash<>(mapper.toDto(applicationWithHash.model()), applicationWithHash.hash());
    }

    public void createApplication(ApplicationDto applicationDto) {
        setDefaultRoleShareResourceLimitIfMissing(applicationDto);
        Optional.of(applicationDto)
                .map(mapper::toDomain)
                .ifPresent(applicationService::createApplication);
    }

    public String updateApplication(String applicationName, ApplicationDto applicationDto, String hash) {
        setDefaultRoleShareResourceLimitIfMissing(applicationDto);
        Application value = mapper.toDomain(applicationDto);
        return applicationService.updateApplication(applicationName, value, hash);
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

    private void setDefaultRoleShareResourceLimitIfMissing(ApplicationDto applicationDto) {
        ShareResourceLimitDto defaultRoleShareResourceLimit = applicationDto.getDefaultRoleShareResourceLimit();
        if (defaultRoleShareResourceLimit == null) {
            defaultRoleShareResourceLimit = new ShareResourceLimitDto();
            defaultRoleShareResourceLimit.setMaxAcceptedUsers(10);
            applicationDto.setDefaultRoleShareResourceLimit(defaultRoleShareResourceLimit);
        }
    }
}