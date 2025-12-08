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
public class ToolSetEximService {
    
    private static final String PUBLIC_FOLDER = "public/";
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

        var toolSetExims = getToolSetExports(distinctPaths);
        return new ToolSetsExim(toolSetExims);
    }

    private List<ToolSetExim> getToolSetExports(List<String> paths) {
        return paths.stream().map(this::getToolSetExport).toList();
    }

    private ToolSetExim getToolSetExport(String path) {
        try {
            var toolSetResource = toolSetResourceService.getToolSetResource(path);
            return toolSetClientMapper.toToolSetExim(toolSetResource);
        } catch (Exception e) {
            log.warn("Cannot load toolSet from path {}", path, e);
            throw new RuntimeException(e);
        }
    }

    public ImportResourcesFileResult importToolSets(ImportResources importToolSets, ToolSetsEximDto toolSetsEximDto) {
        uniquenessValidator.validateToolSetImport(importToolSets, toolSetsEximDto);

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

        return importToolSet(normalizedImportToolSets, toolSetsEximDto, circuitBreaker);
    }

    private ImportResourcesFileResult importToolSet(ImportResources importToolSets,
                                                        ToolSetsEximDto toolSetsEximDto,
                                                        SimpleCircuitBreaker circuitBreaker) {
        try {
            var results = new ArrayList<ImportResourcesResult>();
            for (var toolSet : toolSetsEximDto.getToolSets()) {
                results.add(importToolSet(importToolSets, toolSet, circuitBreaker));
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

    private ImportResourcesResult importToolSet(ImportResources importToolSets,
                                                    ToolSetEximDto toolSetExim,
                                                    SimpleCircuitBreaker circuitBreaker) {
        var toolSetName = EximServiceHelper.getVersionedName(toolSetExim);
        String targetPath;
        if (importToolSets.isFlatImport()) {
            targetPath = importToolSets.getPath() + "/" + toolSetName;
        } else {
            var folderPathWithoutPublic = StringUtils.removeStart(toolSetExim.getFolderId(), PUBLIC_FOLDER);
            targetPath = importToolSets.getPath() + "/" + folderPathWithoutPublic + toolSetName;
        }
        var sourcePath = toolSetExim.getFolderId() == null
                ? toolSetName
                : StringUtils.stripEnd(toolSetExim.getFolderId(), "/") + "/" + toolSetName;
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