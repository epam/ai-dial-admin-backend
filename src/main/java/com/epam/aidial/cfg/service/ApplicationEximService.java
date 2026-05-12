package com.epam.aidial.cfg.service;

import com.epam.aidial.cfg.client.mapper.ApplicationClientMapper;
import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.dto.ApplicationEximDto;
import com.epam.aidial.cfg.dto.ApplicationsEximDto;
import com.epam.aidial.cfg.model.ApplicationExim;
import com.epam.aidial.cfg.model.ApplicationsExim;
import com.epam.aidial.cfg.model.CreateApplicationResource;
import com.epam.aidial.cfg.model.ImportConflictResolutionStrategy;
import com.epam.aidial.cfg.model.ImportResources;
import com.epam.aidial.cfg.model.ImportResourcesFileResult;
import com.epam.aidial.cfg.model.ImportResourcesResult;
import com.epam.aidial.cfg.model.UpdateRulesRequest;
import com.epam.aidial.cfg.utils.EximServiceHelper;
import com.epam.aidial.cfg.utils.ExportPathUtils;
import com.epam.aidial.cfg.utils.PathUtils;
import com.epam.aidial.cfg.utils.ResourceEximExportHelper;
import com.epam.aidial.cfg.utils.ResourceImportPathUtils;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@LogExecution
@RequiredArgsConstructor
public class ApplicationEximService {
    
    private final ApplicationClientMapper applicationClientMapper;
    private final ApplicationResourceService applicationResourceService;
    private final FolderService folderService;
    private final ResourceImportValidator uniquenessValidator;

    @Value("${applications.import.consecutiveErrorsThreshold}")
    private int importErrorsThreshold;

    public ApplicationsExim exportApplications(List<String> paths) {
        var distinctPaths = paths.stream()
                .distinct()
                .sorted()
                .toList();

        var exportEntries = ResourceEximExportHelper.resolveExportEntries(distinctPaths,
                folderPath -> ResourceEximExportHelper.collectPathsUnderFolder(
                        folderPath, applicationResourceService::getApplications, "application"));
        var applicationExims = exportEntries.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> getApplicationExport(e.getKey(), e.getValue()))
                .toList();
        return new ApplicationsExim(applicationExims);
    }

    private ApplicationExim getApplicationExport(String storagePath, String exportFolderPath) {
        try {
            var applicationResource = applicationResourceService.getApplicationResource(storagePath);
            var exportedPath = ExportPathUtils.toExportedVersionedStoragePath(storagePath, exportFolderPath);
            var parts = PathUtils.parseVersionedPath(exportedPath);
            return applicationClientMapper.toApplicationExim(applicationResource, parts);
        } catch (Exception e) {
            log.warn("Cannot load application from path {}", storagePath, e);
            throw new RuntimeException(e);
        }
    }

    public ImportResourcesFileResult importApplications(ImportResources importApplications, ApplicationsEximDto applicationsEximDto) {
        var uniquenessConflicts = uniquenessValidator.collectApplicationUniquenessConflicts(importApplications.isFlatImport(),
                applicationsEximDto);

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

        return importApplications(normalizedImportApplications, applicationsEximDto, circuitBreaker, uniquenessConflicts);
    }

    private ImportResourcesFileResult importApplications(ImportResources importApplications,
                                                         ApplicationsEximDto applicationsEximDto,
                                                         SimpleCircuitBreaker circuitBreaker,
                                                         Map<ResourceLocation, String> uniquenessConflicts) {
        try {
            var results = new ArrayList<ImportResourcesResult>();
            for (var application : applicationsEximDto.getApplications()) {
                var key = ResourceLocation.from(
                        application.getName(),
                        application.getVersion(),
                        application.getFolderId(),
                        importApplications.isFlatImport());
                var conflictMessage = uniquenessConflicts.get(key);
                if (conflictMessage != null) {
                    var paths = ResourceImportPathUtils.resolveVersionedEximImportPaths(
                            importApplications,
                            EximServiceHelper.getVersionedName(application),
                            application.getFolderId());
                    results.add(ImportResourcesResult.createFailure(paths.sourcePath(), paths.targetPath(), conflictMessage));
                    continue;
                }
                results.add(importSingleApplication(importApplications, application, circuitBreaker));
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

    private ImportResourcesResult importSingleApplication(ImportResources importApplications,
                                                          ApplicationEximDto applicationExim,
                                                          SimpleCircuitBreaker circuitBreaker) {
        var paths = ResourceImportPathUtils.resolveVersionedEximImportPaths(
                importApplications,
                EximServiceHelper.getVersionedName(applicationExim),
                applicationExim.getFolderId());
        var sourcePath = paths.sourcePath();
        var targetPath = paths.targetPath();
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
}