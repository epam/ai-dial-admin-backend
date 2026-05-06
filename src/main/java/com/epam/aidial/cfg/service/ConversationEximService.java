package com.epam.aidial.cfg.service;

import com.epam.aidial.cfg.client.mapper.ConversationClientMapper;
import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.dto.ConversationEximDto;
import com.epam.aidial.cfg.dto.ConversationsEximDto;
import com.epam.aidial.cfg.model.ConversationExim;
import com.epam.aidial.cfg.model.ConversationsExim;
import com.epam.aidial.cfg.model.ImportConflictResolutionStrategy;
import com.epam.aidial.cfg.model.ImportResources;
import com.epam.aidial.cfg.model.ImportResourcesFileResult;
import com.epam.aidial.cfg.model.ImportResourcesResult;
import com.epam.aidial.cfg.model.UpdateRulesRequest;
import com.epam.aidial.cfg.utils.EximServiceHelper;
import com.epam.aidial.cfg.utils.ExportPathUtils;
import com.epam.aidial.cfg.utils.PathUtils;
import com.epam.aidial.cfg.utils.ResourceEximExportHelper;
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
public class ConversationEximService {

    private static final String PUBLIC_FOLDER = "public/";

    private final ConversationClientMapper conversationClientMapper;
    private final ConversationService conversationService;
    private final FolderService folderService;
    private final ResourceImportValidator uniquenessValidator;

    @Value("${conversations.import.consecutiveErrorsThreshold}")
    private int importErrorsThreshold;

    public ConversationsExim exportConversations(List<String> paths) {
        var distinctPaths = paths.stream()
                .distinct()
                .sorted()
                .toList();

        var exportEntries = ResourceEximExportHelper.resolveExportEntries(distinctPaths,
                folderPath -> ResourceEximExportHelper.collectPathsUnderFolder(
                        folderPath, conversationService::getConversations, "conversation"));
        var conversationExims = exportEntries.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> getConversationExport(e.getKey(), e.getValue()))
                .toList();
        return new ConversationsExim(conversationExims);
    }

    private ConversationExim getConversationExport(String storagePath, String exportFolderPath) {
        try {
            var conversation = conversationService.getConversation(storagePath);
            var exportedPath = ExportPathUtils.toExportedVersionedStoragePath(storagePath, exportFolderPath);
            var parts = PathUtils.parseVersionedPath(exportedPath);
            return conversationClientMapper.toConversationExim(conversation, parts);
        } catch (Exception e) {
            log.warn("Cannot load conversation from path {}", storagePath, e);
            throw new RuntimeException(e);
        }
    }

    public ImportResourcesFileResult importConversations(ImportResources importConversations, ConversationsEximDto conversationsEximDto) {
        uniquenessValidator.validateConversationImport(importConversations, conversationsEximDto);

        if (importConversations.getRules() != null) {
            var updateRulesRequest = UpdateRulesRequest.builder()
                    .targetFolder(importConversations.getPath())
                    .rules(importConversations.getRules())
                    .build();
            folderService.updatesRules(updateRulesRequest);
        }

        var rootPathStripped = StringUtils.stripEnd(importConversations.getPath(), "/");
        var normalized = ImportResources.builder()
                .path(rootPathStripped)
                .flatImport(importConversations.isFlatImport())
                .conflictResolutionStrategy(importConversations.getConflictResolutionStrategy())
                .rules(importConversations.getRules())
                .build();
        var circuitBreaker = new SimpleCircuitBreaker(importErrorsThreshold);

        return importConversations(normalized, conversationsEximDto, circuitBreaker);
    }

    private ImportResourcesFileResult importConversations(ImportResources importConversations,
                                                          ConversationsEximDto conversationsEximDto,
                                                          SimpleCircuitBreaker circuitBreaker) {
        try {
            var results = new ArrayList<ImportResourcesResult>();
            var items = conversationsEximDto.getConversations() == null
                    ? List.<ConversationEximDto>of()
                    : conversationsEximDto.getConversations();
            for (var conversation : items) {
                results.add(importConversation(importConversations, conversation, circuitBreaker));
            }
            return ImportResourcesFileResult.builder()
                    .importResults(results)
                    .build();
        } catch (Exception ex) {
            log.warn("Conversation file {} import failed", importConversations.getPath(), ex);
            return ImportResourcesFileResult.builder()
                    .importResults(List.of())
                    .error(ex.getMessage())
                    .build();
        }
    }

    private ImportResourcesResult importConversation(ImportResources importConversations,
                                                     ConversationEximDto conversationExim,
                                                     SimpleCircuitBreaker circuitBreaker) {
        var conversationName = EximServiceHelper.getVersionedName(conversationExim);
        String targetPath;
        if (importConversations.isFlatImport()) {
            targetPath = importConversations.getPath() + "/" + conversationName;
        } else {
            var folderPathWithoutPublic = StringUtils.removeStart(conversationExim.getFolderId(), PUBLIC_FOLDER);
            targetPath = importConversations.getPath() + "/" + folderPathWithoutPublic + conversationName;
        }
        var sourcePath = conversationExim.getFolderId() == null
                ? conversationName
                : StringUtils.stripEnd(conversationExim.getFolderId(), "/") + "/" + conversationName;
        try {
            var itemParts = PathUtils.parseVersionedPath(targetPath);
            var conversation = conversationClientMapper.toConversation(conversationExim, itemParts);
            return createConversationWithCircuitBreaker(conversation, sourcePath, targetPath,
                    importConversations.getConflictResolutionStrategy(), circuitBreaker);
        } catch (Exception ex) {
            log.warn("Conversation file {} import failed", importConversations.getPath(), ex);
            return ImportResourcesResult.createFailure(sourcePath, targetPath, ex.getMessage());
        }
    }

    private ImportResourcesResult createConversationWithCircuitBreaker(com.epam.aidial.cfg.model.Conversation conversation,
                                                                       String sourcePath,
                                                                       String targetPath,
                                                                       ImportConflictResolutionStrategy conflictResolutionStrategy,
                                                                       SimpleCircuitBreaker circuitBreaker) {
        return circuitBreaker.apply(
                () -> createConversationOrThrow(conversation, sourcePath, targetPath, conflictResolutionStrategy),
                (ex) -> {
                    log.error("Conversation {} import failed", targetPath, ex);
                    return ImportResourcesResult.createFailure(sourcePath, targetPath, ex.getMessage());
                },
                () -> {
                    log.error("Conversation {} import was skipped due to consecutive errors", targetPath);
                    return ImportResourcesResult.createFailure(sourcePath, targetPath, "Skipped due to consecutive errors");
                }
        );
    }

    private ImportResourcesResult createConversationOrThrow(com.epam.aidial.cfg.model.Conversation conversation,
                                                            String sourcePath,
                                                            String targetPath,
                                                            ImportConflictResolutionStrategy conflictResolutionStrategy) {
        try {
            var allowOverride = conflictResolutionStrategy == ImportConflictResolutionStrategy.OVERRIDE;
            conversationService.putConversation(conversation, allowOverride, null);
            return ImportResourcesResult.createSuccess(sourcePath, targetPath);
        } catch (Exception ex) {
            if (ex instanceof FeignException feignException) {
                if (feignException.status() == 412) {
                    log.debug("Conversation {} import skipped - conversation already exists", targetPath, ex);
                    return ImportResourcesResult.createAlreadyExists(sourcePath, targetPath);
                }
            }
            throw ex;
        }
    }
}