package com.epam.aidial.cfg.service;

import com.epam.aidial.cfg.client.mapper.ApplicationClientMapper;
import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.dto.ApplicationEximDto;
import com.epam.aidial.cfg.dto.ApplicationsEximDto;
import com.epam.aidial.cfg.model.ApplicationExim;
import com.epam.aidial.cfg.model.ApplicationsExim;
import com.epam.aidial.cfg.model.CreateApplicationResource;
import com.epam.aidial.cfg.model.FolderApplicationExim;
import com.epam.aidial.cfg.model.ImportConflictResolutionStrategy;
import com.epam.aidial.cfg.model.ImportResources;
import com.epam.aidial.cfg.model.ImportResourcesFileResult;
import com.epam.aidial.cfg.model.ImportResourcesResult;
import com.epam.aidial.cfg.model.UpdateRulesRequest;
import com.epam.aidial.cfg.utils.PathUtils;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@LogExecution
@RequiredArgsConstructor
public class ApplicationEximService {

    private static final String APPLICATIONS_FOLDER = "applications/";
    private static final String PUBLIC_FOLDER = "public/";
    private final ApplicationClientMapper applicationClientMapper;
    private final ApplicationResourceService applicationResourceService;
    private final FolderService folderService;
    private final ApplicationImportValidator uniquenessValidator;

    @Value("${applications.import.consecutiveErrorsThreshold}")
    private int importErrorsThreshold;

    public ApplicationsExim exportApplications(List<String> paths) {
        var distinctPaths = paths.stream()
                .distinct()
                .sorted()
                .toList();

        var applicationExims = getApplicationExports(distinctPaths);
        var folderExims = getFolderExports(distinctPaths);
        return new ApplicationsExim(applicationExims, folderExims);
    }

    private List<ApplicationExim> getApplicationExports(List<String> paths) {
        return paths.stream().map(this::getApplicationExport).toList();
    }

    private ApplicationExim getApplicationExport(String path) {
        try {
            var applicationResource = applicationResourceService.getApplicationResource(path);
            return applicationClientMapper.toApplicationExim(applicationResource);
        } catch (Exception e) {
            log.warn("Cannot load application from path {}", path, e);
            throw new RuntimeException(e);
        }
    }

    private List<FolderApplicationExim> getFolderExports(List<String> paths) {
        return paths.stream()
                .map(PathUtils::parsePath)
                .map(PathUtils.PathParts::getFolderId)
                .filter(PathUtils::isPathParseable)
                .map(PathUtils::parsePath)
                .map(parts -> FolderApplicationExim.builder()
                        .id(APPLICATIONS_FOLDER + parts.getPath())
                        .name(parts.getName())
                        .type("application")
                        .folderId(APPLICATIONS_FOLDER + parts.getFolderId())
                        .build())
                .toList();
    }

    public ImportResourcesFileResult importApplications(ImportResources importApplications, ApplicationsEximDto applicationsEximDto) {
        uniquenessValidator.validateApplicationImport(importApplications, applicationsEximDto);

        if (importApplications.getRules() != null) {
            var updateRulesRequest = UpdateRulesRequest.builder()
                    .targetFolder(importApplications.getPath())
                    .rules(importApplications.getRules())
                    .build();
            folderService.updatesRules(updateRulesRequest);
        }

        var rootPathStripped = StringUtils.stripEnd(importApplications.getPath(), "/");
        var normalizedImportApplications = ImportResources.builder()
                .path(rootPathStripped)
                .flatImport(importApplications.isFlatImport())
                .conflictResolutionStrategy(importApplications.getConflictResolutionStrategy())
                .rules(importApplications.getRules())
                .build();
        var circuitBreaker = new SimpleCircuitBreaker(importErrorsThreshold);

        return importApplication(normalizedImportApplications, applicationsEximDto, circuitBreaker);
    }

    private ImportResourcesFileResult importApplication(ImportResources importApplications,
                                                        ApplicationsEximDto applicationsEximDto,
                                                        SimpleCircuitBreaker circuitBreaker) {
        try {
            var results = new ArrayList<ImportResourcesResult>();
            for (var application : applicationsEximDto.getApplications()) {
                results.add(importApplication(importApplications, application, circuitBreaker));
            }
            return ImportResourcesFileResult.builder()
                    .importResults(results)
                    .build();
        } catch (Exception ex) {
            log.warn("Application file {} import failed", importApplications.getPath(), ex);
            return ImportResourcesFileResult.builder()
                    .importResults(List.of())
                    .error(ex.getMessage())
                    .build();
        }
    }

    private ImportResourcesResult importApplication(ImportResources importApplications,
                                                    ApplicationEximDto applicationExim,
                                                    SimpleCircuitBreaker circuitBreaker) {
        var applicationName = getVersionedName(applicationExim);
        String targetPath;
        if (importApplications.isFlatImport()) {
            targetPath = importApplications.getPath() + "/" + applicationName;
        } else {
            var folderPathWithoutPublic = StringUtils.removeStart(applicationExim.getFolderId(), PUBLIC_FOLDER);
            targetPath = importApplications.getPath() + "/" + folderPathWithoutPublic + applicationName;
        }
        var sourcePath = applicationExim.getFolderId() == null
                ? applicationName
                : StringUtils.stripEnd(applicationExim.getFolderId(), "/") + "/" + applicationName;
        try {
            var itemParts = PathUtils.parseVersionedPath(targetPath);
            var createApplicationResource = applicationClientMapper.toCreateApplicationResource(applicationExim, itemParts);
            return createApplicationWithCircuitBreaker(createApplicationResource, sourcePath, targetPath,
                    importApplications.getConflictResolutionStrategy(), circuitBreaker);
        } catch (Exception ex) {
            log.warn("Application file {} import failed", importApplications.getPath(), ex);
            return ImportResourcesResult.createFailure(sourcePath, targetPath, ex.getMessage());
        }
    }

    private ImportResourcesResult createApplicationWithCircuitBreaker(CreateApplicationResource createApplicationResource,
                                                                      String sourcePath,
                                                                      String targetPath,
                                                                      ImportConflictResolutionStrategy conflictResolutionStrategy,
                                                                      SimpleCircuitBreaker circuitBreaker) {
        return circuitBreaker.apply(
                () -> createApplicationOrThrow(createApplicationResource, sourcePath, targetPath, conflictResolutionStrategy),
                (ex) -> {
                    log.error("Application {} import failed", targetPath, ex);
                    return ImportResourcesResult.createFailure(sourcePath, targetPath, ex.getMessage());
                },
                () -> {
                    log.error("Application {} import was skipped due to consecutive errors", targetPath);
                    return ImportResourcesResult.createFailure(sourcePath, targetPath, "Skipped due to consecutive errors");
                }
        );
    }

    private ImportResourcesResult createApplicationOrThrow(CreateApplicationResource createApplicationResource,
                                                           String sourcePath,
                                                           String targetPath,
                                                           ImportConflictResolutionStrategy conflictResolutionStrategy) {
        try {
            var allowOverride = conflictResolutionStrategy == ImportConflictResolutionStrategy.OVERRIDE;
            applicationResourceService.putApplicationResource(createApplicationResource, allowOverride, null);
            return ImportResourcesResult.createSuccess(sourcePath, targetPath);
        } catch (Exception ex) {
            if (ex instanceof FeignException feignException) {
                if (feignException.status() == 412) {
                    log.debug("Application {} import skipped - application already exists", targetPath, ex);
                    return ImportResourcesResult.createAlreadyExists(sourcePath, targetPath);
                }
            }
            throw ex;
        }
    }

    public String getVersionedName(ApplicationEximDto applicationEximDto) {
        return applicationEximDto.getDisplayVersion() == null
                ? applicationEximDto.getName()
                : applicationEximDto.getName() + "__" + applicationEximDto.getDisplayVersion();
    }
}