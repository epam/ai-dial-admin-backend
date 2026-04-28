package com.epam.aidial.cfg.service;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.dto.ToolSetEximDto;
import com.epam.aidial.cfg.dto.ToolSetsEximDto;
import com.epam.aidial.cfg.exception.ImportPreviewException;
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
public class ZipToolSetEximService {

    private static final String TOOLSETS_FOLDER = "toolSets/";
    private static final String PUBLIC_FOLDER = "public/";
    private static final String TOOLSETS_FILENAME = "toolSets.json";
    private static final String JSON_FILE_EXTENSION = ".json";
    private static final String TOOLSETS_FULL_PATH = TOOLSETS_FOLDER + TOOLSETS_FILENAME;
    private static final String INVALID_EXPORT_ZIP =
            "Invalid archive format. Please upload a valid aidial-admin archive.";

    private final JsonMapper jsonMapper = JsonMapper.builder()
            .configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, false)
            .build();

    private final ToolSetEximService toolSetEximService;
    private final ResourceImportValidator uniquenessValidator;

    public StreamingResponseBody exportToolSets(List<String> paths) {
        var token = AuthorizationTokenHolder.getToken();

        return outputStream -> {
            try (
                    var ignored = new AuthorizationTokenWrapper(token);
                    var zos = new ZipOutputStream(outputStream)
            ) {
                var toolSetsExim = toolSetEximService.exportToolSets(paths);
                zos.putNextEntry(new ZipEntry(TOOLSETS_FULL_PATH));
                zos.write(jsonMapper.writeValueAsString(toolSetsExim).getBytes());
                zos.closeEntry();
            } catch (Exception e) {
                log.warn("An error occurred while exporting toolSets", e);
                throw e;
            }
        };
    }

    public ImportResourcesFileResult importToolSets(ImportResources importToolSets, MultipartFile zipFile) throws IOException {
        var rootPath = importToolSets.getPath();
        var inputStream = zipFile.getInputStream();

        try (ZipInputStream zipInputStream = new ZipInputStream(inputStream)) {

            ZipEntry zipEntry;
            var toolSetsEximDtos = new HashMap<String, ToolSetsEximDto>();
            while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                var zipEntryName = zipEntry.getName();

                // Validate zip entry path to prevent path traversal attacks
                try {
                    zipEntryName = PathUtils.validateZipEntryPath(zipEntryName);
                } catch (IllegalArgumentException e) {
                    log.warn("Skipping zip entry with invalid path: {}", zipEntryName, e);
                    continue;
                }

                if (!zipEntryName.startsWith(TOOLSETS_FOLDER) || !zipEntryName.endsWith(JSON_FILE_EXTENSION)) {
                    log.warn("Ignoring file {} in zip archive during import. ToolSet import context {}",
                            zipEntryName, importToolSets);
                    continue;
                }
                try {
                    var dto = jsonMapper.readValue(zipInputStream, ToolSetsEximDto.class);
                    toolSetsEximDtos.put(zipEntryName, dto);
                } catch (Exception e) {
                    log.warn("Invalid JSON in zip entry {}. path={}", zipEntryName, rootPath, e);
                    return ImportResourcesFileResult.builder()
                            .importResults(List.of())
                            .error(INVALID_EXPORT_ZIP)
                            .build();
                }
            }

            if (toolSetsEximDtos.isEmpty()) {
                log.warn("No valid tool set entries found in zip. path={}", rootPath);
                return ImportResourcesFileResult.builder()
                        .importResults(List.of())
                        .error(INVALID_EXPORT_ZIP)
                        .build();
            }

            var compacted = compactToolSetsEximDtos(importToolSets, toolSetsEximDtos);
            return toolSetEximService.importToolSets(importToolSets, compacted);
        } catch (Exception ex) {
            log.warn("ToolSet file {} import failed", rootPath, ex);
            return ImportResourcesFileResult.builder()
                    .importResults(List.of())
                    .error(ex.getMessage())
                    .build();
        }
    }

    public ImportResourcesPreview previewImportToolSetsFromZip(ImportResources importToolSets, MultipartFile zipFile) {
        try (ZipInputStream zipInputStream = new ZipInputStream(zipFile.getInputStream())) {

            ZipEntry zipEntry;
            List<ImportResourcePreview> previews = new ArrayList<>();
            while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                var zipEntryName = zipEntry.getName();

                // Validate zip entry path to prevent path traversal attacks
                try {
                    zipEntryName = PathUtils.validateZipEntryPath(zipEntryName);
                } catch (IllegalArgumentException e) {
                    log.warn("Skipping zip entry with invalid path: {}", zipEntryName, e);
                    continue;
                }

                if (zipEntryName.startsWith(TOOLSETS_FOLDER) && zipEntryName.endsWith(JSON_FILE_EXTENSION)) {
                    var toolSetsEximDto = jsonMapper.readValue(zipInputStream, ToolSetsEximDto.class);
                    String finalZipEntryName = zipEntryName;
                    toolSetsEximDto.getToolSets().stream()
                            .map(toolSetEximDto -> getToolSetPathParts(importToolSets, toolSetEximDto))
                            .map(pathParts -> buildImportResourcePreview(pathParts, finalZipEntryName))
                            .forEach(previews::add);
                } else {
                    log.info("Ignoring file {} in zip archive during import preview", zipEntryName);
                }
            }

            return ImportResourcesPreview.builder()
                    .resourcePreviews(previews)
                    .build();
        } catch (Exception ex) {
            log.warn("ToolSet file {} import preview failed", zipFile.getOriginalFilename(), ex);
            throw new ImportPreviewException(String.format("ToolSet file '%s' import preview failed", zipFile.getOriginalFilename()));
        }
    }

    private ToolSetsEximDto compactToolSetsEximDtos(ImportResources importToolSets, HashMap<String, ToolSetsEximDto> fileNameToToolSetsEximDtos) {
        uniquenessValidator.checkToolSetConflicts(importToolSets, fileNameToToolSetsEximDtos);

        var compactedToolSets = fileNameToToolSetsEximDtos.values().stream()
                .map(ToolSetsEximDto::getToolSets)
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .collect(Collectors.toList());

        return ToolSetsEximDto.builder()
                .toolSets(compactedToolSets)
                .build();
    }

    private PathUtils.VersionedPathParts getToolSetPathParts(ImportResources importToolSets, ToolSetEximDto toolSet) {
        var rootPath = importToolSets.getPath();
        var rootPathStripped = StringUtils.stripEnd(rootPath, "/");

        var folderPathWithoutPublic = StringUtils.removeStart(toolSet.getFolderId(), PUBLIC_FOLDER);
        var toolSetName = EximServiceHelper.getVersionedName(toolSet);
        var targetPath = rootPathStripped + "/" + folderPathWithoutPublic + toolSetName;

        return PathUtils.parseVersionedPath(targetPath);
    }

    private ImportResourcePreview buildImportResourcePreview(PathUtils.VersionedPathParts toolSetPathParts, String fileName) {
        return ImportResourcePreview.builder()
                .name(toolSetPathParts.getName())
                .version(toolSetPathParts.getVersion())
                .fileName(fileName)
                .build();
    }
    
}