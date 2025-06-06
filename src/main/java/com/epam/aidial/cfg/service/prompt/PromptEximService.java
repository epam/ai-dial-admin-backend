package com.epam.aidial.cfg.service.prompt;

import com.epam.aidial.cfg.client.PromptClient;
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
import com.epam.aidial.cfg.service.SimpleCircuitBreaker;
import com.epam.aidial.cfg.utils.PathUtils;
import feign.FeignException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Validated
@Service
@LogExecution
@RequiredArgsConstructor
public class PromptEximService {

    private static final String PROMPTS_FOLDER = "prompts/";
    private static final String PUBLIC_FOLDER = "public/";

    private final PromptClient promptClient;
    private final PromptClientMapper promptClientMapper;
    private final PromptService promptService;
    private final ObjectFactory<SimpleCircuitBreaker> circuitBreakerFactory;

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
            var promptDto = promptClient.getPrompt(path);

            var parts = PathUtils.parseVersionedPath(path);
            promptDto.setId(PROMPTS_FOLDER + parts.getPath());
            promptDto.setName(parts.getName());
            promptDto.setFolderId(PROMPTS_FOLDER + parts.getFolderId());

            return promptClientMapper.toPromptExim(promptDto);
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

    public ImportResourcesFileResult importPrompts(ImportResources importPrompts, @Valid PromptsEximDto promptsEximDto) {
        var rootPath = importPrompts.getPath();
        var rootPathStripped = StringUtils.stripEnd(rootPath, "/");
        var conflictResolutionStrategy = importPrompts.getConflictResolutionStrategy();
        var circuitBreaker = circuitBreakerFactory.getObject();

        return importPrompt(rootPathStripped, promptsEximDto, conflictResolutionStrategy, circuitBreaker);
    }

    private ImportResourcesFileResult importPrompt(String rootPath,
                                                   PromptsEximDto promptsEximDto,
                                                   ImportConflictResolutionStrategy conflictResolutionStrategy,
                                                   SimpleCircuitBreaker circuitBreaker) {
        try {
            var results = new ArrayList<ImportResourcesResult>();
            for (var prompt : promptsEximDto.getPrompts()) {
                results.add(importPrompt(rootPath, prompt, conflictResolutionStrategy, circuitBreaker));
            }
            return ImportResourcesFileResult.builder()
                    .importResults(results)
                    .build();
        } catch (Exception ex) {
            log.debug("Prompt file {} import failed", rootPath, ex);
            return ImportResourcesFileResult.builder()
                    .importResults(List.of())
                    .error(ex.getMessage())
                    .build();
        }
    }

    private ImportResourcesResult importPrompt(String rootPath,
                                               PromptEximDto promptExim,
                                               ImportConflictResolutionStrategy conflictResolutionStrategy,
                                               SimpleCircuitBreaker circuitBreaker) {
        var rawPath = promptExim.getId();
        var sourcePath = StringUtils.removeStart(rawPath, PROMPTS_FOLDER);
        var sourcePathWithoutPublic = StringUtils.removeStart(sourcePath, PUBLIC_FOLDER);
        var targetPath = rootPath + "/" + sourcePathWithoutPublic;

        try {
            var itemParts = PathUtils.parseVersionedPath(targetPath);
            var createPrompt = CreatePrompt.builder()
                    .name(itemParts.getName())
                    .version(itemParts.getVersion())
                    .folderId(itemParts.getFolderId())
                    .description(promptExim.getDescription())
                    .content(promptExim.getContent())
                    .build();
            return createPromptWithCircuitBreaker(createPrompt, sourcePath, targetPath, conflictResolutionStrategy,
                    circuitBreaker);
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
                    if (ex != null) {
                        log.error("Prompt {} import failed", targetPath, ex);
                        return ImportResourcesResult.createFailure(sourcePath, targetPath, ex.getMessage());
                    } else {
                        log.error("Prompt {} import was skipped due to consecutive errors", targetPath);
                        return ImportResourcesResult.createFailure(sourcePath, targetPath, "Skipped due to consecutive errors");
                    }
                }
        );
    }

    private ImportResourcesResult createPromptOrThrow(CreatePrompt createPrompt,
                                                      String sourcePath,
                                                      String targetPath,
                                                      ImportConflictResolutionStrategy conflictResolutionStrategy) {
        try {
            var allowOverride = conflictResolutionStrategy == ImportConflictResolutionStrategy.OVERRIDE;
            promptService.createPrompt(createPrompt, allowOverride, null);
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
