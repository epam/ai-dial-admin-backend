package com.epam.aidial.cfg.service.prompt;

import com.epam.aidial.cfg.client.mapper.PromptClientMapper;
import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.dto.PromptEximDto;
import com.epam.aidial.cfg.dto.PromptsEximDto;
import com.epam.aidial.cfg.model.CreatePrompt;
import com.epam.aidial.cfg.model.FolderExim;
import com.epam.aidial.cfg.model.ImportConflictResolutionStrategy;
import com.epam.aidial.cfg.model.ImportResources;
import com.epam.aidial.cfg.model.ImportResourcesFileResult;
import com.epam.aidial.cfg.model.ImportResourcesResult;
import com.epam.aidial.cfg.model.PromptExim;
import com.epam.aidial.cfg.model.PromptsExim;
import com.epam.aidial.cfg.model.UpdateRulesRequest;
import com.epam.aidial.cfg.service.FolderService;
import com.epam.aidial.cfg.service.SimpleCircuitBreaker;
import com.epam.aidial.cfg.utils.PathUtils;
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
public class PromptEximService {

    private static final String PROMPTS_FOLDER = "prompts/";
    private static final String PUBLIC_FOLDER = "public/";

    private final PromptClientMapper promptClientMapper;
    private final PromptService promptService;
    private final FolderService folderService;
    private final PromptImportValidator uniquenessValidator;

    @Value("${prompts.import.consecutiveErrorsThreshold}")
    private int importErrorsThreshold;

    public PromptsExim exportPrompts(List<String> paths) {
        var distinctPaths = paths.stream()
                .distinct()
                .sorted()
                .toList();

        var promptExims = getPromptExports(distinctPaths);
        var folderExims = getFolderExports(distinctPaths);
        return new PromptsExim(promptExims, folderExims);
    }

    private List<PromptExim> getPromptExports(List<String> paths) {
        return paths.stream().map(this::getPromptExport).toList();
    }

    private PromptExim getPromptExport(String path) {
        try {
            var prompt = promptService.getPrompt(path);
            var parts = PathUtils.parseVersionedPath(path);
            return promptClientMapper.toPromptExim(prompt, parts);
        } catch (Exception e) {
            log.error("Cannot load prompt from path {}", path, e);
            throw new RuntimeException(e);
        }
    }

    private List<FolderExim> getFolderExports(List<String> paths) {
        return paths.stream()
                .map(PathUtils::parsePath)
                .map(PathUtils.PathParts::getFolderId)
                .filter(PathUtils::isPathParseable)
                .map(PathUtils::parsePath)
                .map(parts -> FolderExim.builder()
                        .id(PROMPTS_FOLDER + parts.getPath())
                        .name(parts.getName())
                        .type("prompt")
                        .folderId(PROMPTS_FOLDER + parts.getFolderId())
                        .build())
                .toList();
    }

    public ImportResourcesFileResult importPrompts(ImportResources importPrompts, PromptsEximDto promptsEximDto) {
        var uniquenessConflicts = uniquenessValidator.collectUniquenessConflicts(importPrompts, promptsEximDto);

        if (importPrompts.getRules() != null) {
            var updateRulesRequest = UpdateRulesRequest.builder()
                    .targetFolder(importPrompts.getPath())
                    .rules(importPrompts.getRules())
                    .build();
            folderService.updatesRules(updateRulesRequest);
        }

        var rootPathStripped = StringUtils.stripEnd(importPrompts.getPath(), "/");
        var normalizedImportPrompts = ImportResources.builder()
                .path(rootPathStripped)
                .flatImport(importPrompts.isFlatImport())
                .conflictResolutionStrategy(importPrompts.getConflictResolutionStrategy())
                .rules(importPrompts.getRules())
                .build();
        var circuitBreaker = new SimpleCircuitBreaker(importErrorsThreshold);

        return importPrompt(normalizedImportPrompts, promptsEximDto, circuitBreaker, uniquenessConflicts);
    }

    private ImportResourcesFileResult importPrompt(ImportResources importPrompts,
                                                   PromptsEximDto promptsEximDto,
                                                   SimpleCircuitBreaker circuitBreaker,
                                                   Map<String, String> uniquenessConflicts) {
        try {
            var results = new ArrayList<ImportResourcesResult>();
            for (var prompt : promptsEximDto.getPrompts()) {
                var conflictMessage = uniquenessConflicts.get(prompt.getId());
                if (conflictMessage != null) {
                    var paths = resolveImportPaths(importPrompts, prompt);
                    results.add(ImportResourcesResult.createFailure(paths.sourcePath(), paths.targetPath(), conflictMessage));
                    continue;
                }
                results.add(importSinglePrompt(importPrompts, prompt, circuitBreaker));
            }
            return ImportResourcesFileResult.builder()
                    .importResults(results)
                    .build();
        } catch (Exception ex) {
            log.debug("Prompt file {} import failed", importPrompts.getPath(), ex);
            return ImportResourcesFileResult.builder()
                    .importResults(List.of())
                    .error(ex.getMessage())
                    .build();
        }
    }

    private ImportPromptPaths resolveImportPaths(ImportResources importPrompts, PromptEximDto promptExim) {
        var rawPath = promptExim.getId();
        var sourcePath = StringUtils.removeStart(rawPath, PROMPTS_FOLDER);
        String targetPath;
        if (importPrompts.isFlatImport()) {
            var promptName = PathUtils.parseVersionedPath(sourcePath).getVersionedName();
            targetPath = importPrompts.getPath() + "/" + promptName;
        } else {
            var sourcePathWithoutPublic = StringUtils.removeStart(sourcePath, PUBLIC_FOLDER);
            targetPath = importPrompts.getPath() + "/" + sourcePathWithoutPublic;
        }
        return new ImportPromptPaths(sourcePath, targetPath);
    }

    private ImportResourcesResult importSinglePrompt(ImportResources importPrompts,
                                                     PromptEximDto promptExim,
                                                     SimpleCircuitBreaker circuitBreaker) {

        var paths = resolveImportPaths(importPrompts, promptExim);
        var sourcePath = paths.sourcePath();
        var targetPath = paths.targetPath();

        try {
            var itemParts = PathUtils.parseVersionedPath(targetPath);
            var createPrompt = CreatePrompt.builder()
                    .name(itemParts.getName())
                    .version(itemParts.getVersion())
                    .folderId(itemParts.getFolderId())
                    .description(promptExim.getDescription())
                    .content(promptExim.getContent())
                    .build();
            return createPromptWithCircuitBreaker(createPrompt, sourcePath, targetPath,
                    importPrompts.getConflictResolutionStrategy(), circuitBreaker);
        } catch (Exception ex) {
            return ImportResourcesResult.createFailure(sourcePath, targetPath, ex.getMessage());
        }
    }

    private ImportResourcesResult createPromptWithCircuitBreaker(CreatePrompt createPrompt,
                                                                 String sourcePath,
                                                                 String targetPath,
                                                                 ImportConflictResolutionStrategy conflictResolutionStrategy,
                                                                 SimpleCircuitBreaker circuitBreaker) {
        return circuitBreaker.apply(
                () -> createPromptOrThrow(createPrompt, sourcePath, targetPath, conflictResolutionStrategy),
                (ex) -> {
                    log.error("Prompt {} import failed", targetPath, ex);
                    return ImportResourcesResult.createFailure(sourcePath, targetPath, ex.getMessage());
                },
                () -> {
                    log.error("Prompt {} import was skipped due to consecutive errors", targetPath);
                    return ImportResourcesResult.createFailure(sourcePath, targetPath, "Skipped due to consecutive errors");
                }
        );
    }

    private ImportResourcesResult createPromptOrThrow(CreatePrompt createPrompt,
                                                      String sourcePath,
                                                      String targetPath,
                                                      ImportConflictResolutionStrategy conflictResolutionStrategy) {
        try {
            if (conflictResolutionStrategy == ImportConflictResolutionStrategy.SKIP) {
                promptService.createPrompt(createPrompt);
            } else {
                promptService.putPrompt(createPrompt, true, null);
            }
            return ImportResourcesResult.createSuccess(sourcePath, targetPath);
        } catch (Exception ex) {
            if (ex instanceof FeignException feignException) {
                if (feignException.status() == 412) {
                    log.debug("Prompt {} import skipped - prompt already exists", targetPath, ex);
                    return ImportResourcesResult.createAlreadyExists(sourcePath, targetPath);
                }
            }
            throw ex;
        }
    }

}