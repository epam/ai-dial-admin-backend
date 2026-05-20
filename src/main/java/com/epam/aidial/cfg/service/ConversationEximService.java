package com.epam.aidial.cfg.service;

import com.epam.aidial.cfg.client.mapper.ConversationClientMapper;
import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.dto.ConversationEximDto;
import com.epam.aidial.cfg.dto.ConversationsEximDto;
import com.epam.aidial.cfg.exception.OptimisticLockConflictException;
import com.epam.aidial.cfg.model.Conversation;
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
import com.epam.aidial.cfg.utils.ResourceImportPathUtils;
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
        var uniquenessConflicts = uniquenessValidator.collectConversationUniquenessConflicts(importConversations.isFlatImport(), conversationsEximDto);

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

        return importConversations(normalized, conversationsEximDto, circuitBreaker, uniquenessConflicts);
    }

    private ImportResourcesFileResult importConversations(ImportResources importConversations,
                                                          ConversationsEximDto conversationsEximDto,
                                                          SimpleCircuitBreaker circuitBreaker,
                                                          Map<ResourceLocation, String> uniquenessConflicts) {
        try {
            var results = new ArrayList<ImportResourcesResult>();
            for (var conversation : conversationsEximDto.getConversations()) {
                var key = ResourceLocation.from(
                        conversation.getName(),
                        conversation.getVersion(),
                        conversation.getFolderId(),
                        importConversations.isFlatImport());
                var conflictMessage = uniquenessConflicts.get(key);
                if (conflictMessage != null) {
                    var paths = ResourceImportPathUtils.resolveVersionedEximImportPaths(
                            importConversations,
                            EximServiceHelper.getVersionedName(conversation),
                            conversation.getFolderId());
                    results.add(ImportResourcesResult.createFailure(paths.sourcePath(), paths.targetPath(), conflictMessage));
                    continue;
                }
                results.add(importSingleConversation(importConversations, conversation, circuitBreaker));
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

    private ImportResourcesResult importSingleConversation(ImportResources importConversations,
                                                           ConversationEximDto conversationExim,
                                                           SimpleCircuitBreaker circuitBreaker) {
        var paths = ResourceImportPathUtils.resolveVersionedEximImportPaths(
                importConversations,
                EximServiceHelper.getVersionedName(conversationExim),
                conversationExim.getFolderId());

        var sourcePath = paths.sourcePath();
        var targetPath = paths.targetPath();
        try {
            var itemParts = PathUtils.parseVersionedPath(targetPath);
            var createConversation = conversationClientMapper.toConversation(conversationExim, itemParts);
            return createConversationWithCircuitBreaker(createConversation, sourcePath, targetPath,
                    importConversations.getConflictResolutionStrategy(), circuitBreaker);
        } catch (Exception ex) {
            log.warn("Conversation file {} import failed", importConversations.getPath(), ex);
            return ImportResourcesResult.createFailure(sourcePath, targetPath, ex.getMessage());
        }
    }

    private ImportResourcesResult createConversationWithCircuitBreaker(Conversation conversation,
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

    private ImportResourcesResult createConversationOrThrow(Conversation conversation,
                                                            String sourcePath,
                                                            String targetPath,
                                                            ImportConflictResolutionStrategy conflictResolutionStrategy) {
        try {
            var allowOverride = conflictResolutionStrategy == ImportConflictResolutionStrategy.OVERRIDE;
            conversationService.putConversation(conversation, allowOverride, null);
            return ImportResourcesResult.createSuccess(sourcePath, targetPath);
        } catch (OptimisticLockConflictException ex) {
            if (conflictResolutionStrategy == ImportConflictResolutionStrategy.SKIP) {
                log.debug("Conversation {} import skipped - conversation already exists", targetPath, ex);
                return ImportResourcesResult.createSkip(sourcePath, targetPath);
            }
            throw ex;
        }
    }
}