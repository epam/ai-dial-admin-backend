package com.epam.aidial.cfg.service;

import com.epam.aidial.cfg.client.mapper.ToolSetClientMapper;
import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.dto.ToolSetEximDto;
import com.epam.aidial.cfg.dto.ToolSetsEximDto;
import com.epam.aidial.cfg.model.CreateToolSetResource;
import com.epam.aidial.cfg.model.ImportConflictResolutionStrategy;
import com.epam.aidial.cfg.model.ImportResources;
import com.epam.aidial.cfg.model.ImportResourcesFileResult;
import com.epam.aidial.cfg.model.ImportResourcesResult;
import com.epam.aidial.cfg.model.ToolSetExim;
import com.epam.aidial.cfg.model.ToolSetsExim;
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
public class ToolSetEximService {
    
    private final ToolSetClientMapper toolSetClientMapper;
    private final ToolSetResourceService toolSetResourceService;
    private final FolderService folderService;
    private final ResourceImportValidator uniquenessValidator;

    @Value("${toolsets.import.consecutiveErrorsThreshold}")
    private int importErrorsThreshold;

    public ToolSetsExim exportToolSets(List<String> paths) {
        var distinctPaths = paths.stream()
                .distinct()
                .sorted()
                .toList();

        var exportEntries = ResourceEximExportHelper.resolveExportEntries(distinctPaths,
                folderPath -> ResourceEximExportHelper.collectPathsUnderFolder(
                        folderPath, toolSetResourceService::getToolSetResources, "toolSet"));
        var toolSetExims = exportEntries.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> getToolSetExport(e.getKey(), e.getValue()))
                .toList();
        return new ToolSetsExim(toolSetExims);
    }

    private ToolSetExim getToolSetExport(String storagePath, String exportFolderPath) {
        try {
            var toolSetResource = toolSetResourceService.getToolSetResource(storagePath);
            var exportedPath = ExportPathUtils.toExportedVersionedStoragePath(storagePath, exportFolderPath);
            var parts = PathUtils.parseVersionedPath(exportedPath);
            return toolSetClientMapper.toToolSetExim(toolSetResource, parts);
        } catch (Exception e) {
            log.warn("Cannot load toolSet from path {}", storagePath, e);
            throw new RuntimeException(e);
        }
    }

    public ImportResourcesFileResult importToolSets(ImportResources importToolSets, ToolSetsEximDto toolSetsEximDto) {
        var uniquenessConflicts = uniquenessValidator.collectToolSetUniquenessConflicts(importToolSets.isFlatImport(), toolSetsEximDto);

        if (importToolSets.getRules() != null) {
            var updateRulesRequest = UpdateRulesRequest.builder()
                    .targetFolder(importToolSets.getPath())
                    .rules(importToolSets.getRules())
                    .build();
            folderService.updatesRules(updateRulesRequest);
        }

        var rootPathStripped = StringUtils.stripEnd(importToolSets.getPath(), "/");
        var normalizedImportToolSets = ImportResources.builder()
                .path(rootPathStripped)
                .flatImport(importToolSets.isFlatImport())
                .conflictResolutionStrategy(importToolSets.getConflictResolutionStrategy())
                .rules(importToolSets.getRules())
                .build();
        var circuitBreaker = new SimpleCircuitBreaker(importErrorsThreshold);

        return importToolSets(normalizedImportToolSets, toolSetsEximDto, circuitBreaker, uniquenessConflicts);
    }

    private ImportResourcesFileResult importToolSets(ImportResources importToolSets,
                                                     ToolSetsEximDto toolSetsEximDto,
                                                     SimpleCircuitBreaker circuitBreaker,
                                                     Map<ResourceNameAndVersionAndPath, String> uniquenessConflicts) {
        try {
            var results = new ArrayList<ImportResourcesResult>();
            for (var toolSet : toolSetsEximDto.getToolSets()) {
                var key = ResourceNameAndVersionAndPath.from(
                        toolSet.getName(),
                        toolSet.getVersion(),
                        toolSet.getFolderId(),
                        importToolSets.isFlatImport());
                var conflictMessage = uniquenessConflicts.get(key);
                if (conflictMessage != null) {
                    var paths = ResourceImportPathUtils.resolveVersionedEximImportPaths(
                            importToolSets,
                            EximServiceHelper.getVersionedName(toolSet),
                            toolSet.getFolderId());
                    results.add(ImportResourcesResult.createFailure(paths.sourcePath(), paths.targetPath(), conflictMessage));
                    continue;
                }
                results.add(importSingleToolSet(importToolSets, toolSet, circuitBreaker));
            }
            return ImportResourcesFileResult.builder()
                    .importResults(results)
                    .build();
        } catch (Exception ex) {
            log.warn("ToolSet file {} import failed", importToolSets.getPath(), ex);
            return ImportResourcesFileResult.builder()
                    .importResults(List.of())
                    .error(ex.getMessage())
                    .build();
        }
    }

    private ImportResourcesResult importSingleToolSet(ImportResources importToolSets,
                                                      ToolSetEximDto toolSetExim,
                                                      SimpleCircuitBreaker circuitBreaker) {
        var paths = ResourceImportPathUtils.resolveVersionedEximImportPaths(
                importToolSets,
                EximServiceHelper.getVersionedName(toolSetExim),
                toolSetExim.getFolderId());
        var sourcePath = paths.sourcePath();
        var targetPath = paths.targetPath();
        try {
            var itemParts = PathUtils.parseVersionedPath(targetPath);
            var createToolSetResource = toolSetClientMapper.toCreateToolSetResource(toolSetExim, itemParts);
            return createToolSetWithCircuitBreaker(createToolSetResource, sourcePath, targetPath,
                    importToolSets.getConflictResolutionStrategy(), circuitBreaker);
        } catch (Exception ex) {
            log.warn("ToolSet file {} import failed", importToolSets.getPath(), ex);
            return ImportResourcesResult.createFailure(sourcePath, targetPath, ex.getMessage());
        }
    }

    private ImportResourcesResult createToolSetWithCircuitBreaker(CreateToolSetResource createToolSetResource,
                                                                  String sourcePath,
                                                                  String targetPath,
                                                                  ImportConflictResolutionStrategy conflictResolutionStrategy,
                                                                  SimpleCircuitBreaker circuitBreaker) {
        return circuitBreaker.apply(
                () -> createToolSetOrThrow(createToolSetResource, sourcePath, targetPath, conflictResolutionStrategy),
                (ex) -> {
                    log.error("ToolSet {} import failed", targetPath, ex);
                    return ImportResourcesResult.createFailure(sourcePath, targetPath, ex.getMessage());
                },
                () -> {
                    log.error("ToolSet {} import was skipped due to consecutive errors", targetPath);
                    return ImportResourcesResult.createFailure(sourcePath, targetPath, "Skipped due to consecutive errors");
                }
        );
    }

    private ImportResourcesResult createToolSetOrThrow(CreateToolSetResource createToolSetResource,
                                                       String sourcePath,
                                                       String targetPath,
                                                       ImportConflictResolutionStrategy conflictResolutionStrategy) {
        try {
            var allowOverride = conflictResolutionStrategy == ImportConflictResolutionStrategy.OVERRIDE;
            toolSetResourceService.putToolSetResource(createToolSetResource, allowOverride, null);
            return ImportResourcesResult.createSuccess(sourcePath, targetPath);
        } catch (Exception ex) {
            if (ex instanceof FeignException feignException) {
                if (feignException.status() == 412) {
                    log.debug("ToolSet {} import skipped - toolSet already exists", targetPath, ex);
                    return ImportResourcesResult.createAlreadyExists(sourcePath, targetPath);
                }
            }
            throw ex;
        }
    }
}