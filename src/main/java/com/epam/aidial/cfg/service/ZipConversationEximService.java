package com.epam.aidial.cfg.service;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.dto.ConversationEximDto;
import com.epam.aidial.cfg.dto.ConversationsEximDto;
import com.epam.aidial.cfg.exception.ImportPreviewException;
import com.epam.aidial.cfg.mapper.ConversationMapper;
import com.epam.aidial.cfg.model.ImportResourcePreview;
import com.epam.aidial.cfg.model.ImportResources;
import com.epam.aidial.cfg.model.ImportResourcesFileResult;
import com.epam.aidial.cfg.model.ImportResourcesPreview;
import com.epam.aidial.cfg.security.AuthorizationTokenHolder;
import com.epam.aidial.cfg.security.AuthorizationTokenWrapper;
import com.epam.aidial.cfg.utils.EximServiceHelper;
import com.epam.aidial.cfg.utils.PathUtils;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

@Slf4j
@Service
@LogExecution
@RequiredArgsConstructor
public class ZipConversationEximService {

    private static final String CONVERSATIONS_FOLDER = "conversations/";
    private static final String PUBLIC_FOLDER = "public/";
    private static final String CONVERSATIONS_FILENAME = "conversations.json";
    private static final String JSON_FILE_EXTENSION = ".json";
    private static final String CONVERSATIONS_FULL_PATH = CONVERSATIONS_FOLDER + CONVERSATIONS_FILENAME;
    private static final String INVALID_EXPORT_ZIP =
            "Invalid archive format. Please upload a valid aidial-admin archive.";

    private final ResourceImportValidator uniquenessValidator;

    private final JsonMapper jsonMapper = JsonMapper.builder()
            .configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, false)
            .build();

    private final ConversationEximService conversationEximService;

    private final ConversationMapper conversationResourceMapper;

    public StreamingResponseBody exportConversations(List<String> paths) {
        var token = AuthorizationTokenHolder.getToken();

        return outputStream -> {
            try (
                    var ignored = new AuthorizationTokenWrapper(token);
                    var zos = new ZipOutputStream(outputStream)
            ) {
                var conversationsExim = conversationEximService.exportConversations(paths);
                var conversationsEximDto = conversationResourceMapper.toConversationsEximDto(conversationsExim);
                zos.putNextEntry(new ZipEntry(CONVERSATIONS_FULL_PATH));
                zos.write(
                        jsonMapper.writeValueAsString(conversationsEximDto)
                                .getBytes(StandardCharsets.UTF_8)
                );
                zos.closeEntry();
            } catch (Exception e) {
                log.warn("An error occurred while exporting conversations", e);
                throw e;
            }
        };
    }

    public ImportResourcesFileResult importConversations(ImportResources importConversations, MultipartFile zipFile) throws IOException {
        var rootPath = importConversations.getPath();
        var inputStream = zipFile.getInputStream();

        try (ZipInputStream zipInputStream = new ZipInputStream(inputStream)) {

            ZipEntry zipEntry;
            var conversationsEximDtos = new HashMap<String, ConversationsEximDto>();
            while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                var zipEntryName = zipEntry.getName();

                try {
                    zipEntryName = PathUtils.validateZipEntryPath(zipEntryName);
                } catch (IllegalArgumentException e) {
                    log.warn("Skipping zip entry with invalid path: {}", zipEntryName, e);
                    continue;
                }

                if (!zipEntryName.startsWith(CONVERSATIONS_FOLDER) || !zipEntryName.endsWith(JSON_FILE_EXTENSION)) {
                    log.warn("Ignoring file {} in zip archive during import. context={}",
                            zipEntryName, importConversations);
                    continue;
                }
                try {
                    var dto = jsonMapper.readValue(zipInputStream, ConversationsEximDto.class);
                    conversationsEximDtos.put(zipEntryName, dto);
                } catch (Exception e) {
                    log.warn("Invalid JSON in zip entry {}. path={}", zipEntryName, rootPath, e);
                    return ImportResourcesFileResult.builder()
                            .importResults(List.of())
                            .error(INVALID_EXPORT_ZIP)
                            .build();
                }
            }
            if (conversationsEximDtos.isEmpty()) {
                log.warn("No valid conversation entries found in zip. path={}", rootPath);
                return ImportResourcesFileResult.builder()
                        .importResults(List.of())
                        .error(INVALID_EXPORT_ZIP)
                        .build();
            }
            var compacted = compactConversationsEximDtos(importConversations, conversationsEximDtos);
            return conversationEximService.importConversations(importConversations, compacted);
        } catch (Exception ex) {
            log.warn("Conversation file {} import failed", rootPath, ex);
            return ImportResourcesFileResult.builder()
                    .importResults(List.of())
                    .error(ex.getMessage())
                    .build();
        }
    }

    public ImportResourcesPreview previewImportConversationsFromZip(ImportResources importConversations, MultipartFile zipFile) {
        try (ZipInputStream zipInputStream = new ZipInputStream(zipFile.getInputStream())) {

            ZipEntry zipEntry;
            List<ImportResourcePreview> previews = new ArrayList<>();
            while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                var zipEntryName = zipEntry.getName();

                try {
                    zipEntryName = PathUtils.validateZipEntryPath(zipEntryName);
                } catch (IllegalArgumentException e) {
                    log.warn("Skipping zip entry with invalid path: {}", zipEntryName, e);
                    continue;
                }

                if (zipEntryName.startsWith(CONVERSATIONS_FOLDER) && zipEntryName.endsWith(JSON_FILE_EXTENSION)) {
                    var conversationsEximDto = jsonMapper.readValue(zipInputStream, ConversationsEximDto.class);
                    String finalZipEntryName = zipEntryName;
                    var list = conversationsEximDto.getConversations();
                    if (list != null) {
                        list.stream()
                                .map(conversationEximDto -> getConversationPathParts(importConversations, conversationEximDto))
                                .map(pathParts -> buildImportResourcePreview(pathParts, finalZipEntryName))
                                .forEach(previews::add);
                    }
                } else {
                    log.info("Ignoring file {} in zip archive during import preview", zipEntryName);
                }
            }

            return ImportResourcesPreview.builder()
                    .resourcePreviews(previews)
                    .build();
        } catch (Exception ex) {
            log.warn("Conversation file {} import preview failed", zipFile.getOriginalFilename(), ex);
            throw new ImportPreviewException(String.format("Conversation file '%s' import preview failed", zipFile.getOriginalFilename()));
        }
    }

    private ConversationsEximDto compactConversationsEximDtos(ImportResources importConversations,
                                                              HashMap<String, ConversationsEximDto> fileNameToDtos) {
        uniquenessValidator.checkConversationConflicts(importConversations, fileNameToDtos);

        var compacted = fileNameToDtos.values().stream()
                .map(ConversationsEximDto::getConversations)
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .collect(Collectors.toList());

        return ConversationsEximDto.builder()
                .conversations(compacted)
                .build();
    }

    private PathUtils.VersionedPathParts getConversationPathParts(ImportResources importConversations, ConversationEximDto conversation) {
        var rootPath = importConversations.getPath();
        var rootPathStripped = StringUtils.stripEnd(rootPath, "/");

        var folderPathWithoutPublic = StringUtils.removeStart(conversation.getFolderId(), PUBLIC_FOLDER);
        var conversationName = EximServiceHelper.getVersionedName(conversation);
        var targetPath = rootPathStripped + "/" + folderPathWithoutPublic + conversationName;

        return PathUtils.parseVersionedPath(targetPath);
    }

    private ImportResourcePreview buildImportResourcePreview(PathUtils.VersionedPathParts pathParts, String fileName) {
        return ImportResourcePreview.builder()
                .name(pathParts.getName())
                .version(pathParts.getVersion())
                .fileName(fileName)
                .build();
    }
}