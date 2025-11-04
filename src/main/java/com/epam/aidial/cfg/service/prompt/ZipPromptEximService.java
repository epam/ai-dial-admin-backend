package com.epam.aidial.cfg.service.prompt;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.dto.PromptEximDto;
import com.epam.aidial.cfg.dto.PromptsEximDto;
import com.epam.aidial.cfg.model.ImportResourcePreview;
import com.epam.aidial.cfg.model.ImportResources;
import com.epam.aidial.cfg.model.ImportResourcesFileResult;
import com.epam.aidial.cfg.model.ImportResourcesPreview;
import com.epam.aidial.cfg.security.AuthorizationTokenHolder;
import com.epam.aidial.cfg.security.AuthorizationTokenWrapper;
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

@Slf4j
@Service
@LogExecution
@RequiredArgsConstructor
public class ZipPromptEximService {

    private static final String PROMPTS_FOLDER = "prompts/";
    private static final String PUBLIC_FOLDER = "public/";
    private static final String PROMPTS_FILENAME = "prompts.json";
    private static final String JSON_FILE_EXTENSION = ".json";
    private static final String PROMPTS_FULL_PATH = PROMPTS_FOLDER + PROMPTS_FILENAME;

    private final JsonMapper jsonMapper = JsonMapper.builder()
            .configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, false)
            .build();

    private final PromptEximService promptEximService;

    public StreamingResponseBody exportPrompts(List<String> paths) {
        var token = AuthorizationTokenHolder.getToken();

        return outputStream -> {
            try (
                    var ignored = new AuthorizationTokenWrapper(token);
                    var zos = new ZipOutputStream(outputStream)
            ) {
                var promptsExim = promptEximService.exportPrompts(paths);
                zos.putNextEntry(new ZipEntry(PROMPTS_FULL_PATH));
                zos.write(jsonMapper.writeValueAsString(promptsExim).getBytes());
                zos.closeEntry();
            } catch (Exception e) {
                log.error("An error occurred while exporting prompts", e);
                throw e;
            }
        };
    }

    public ImportResourcesFileResult importPrompts(ImportResources importPrompts, MultipartFile zipFile) throws IOException {
        var rootPath = importPrompts.getPath();
        var inputStream = zipFile.getInputStream();

        try (ZipInputStream zipInputStream = new ZipInputStream(inputStream)) {

            ZipEntry zipEntry;
            var promptsEximDtos = new HashMap<String, PromptsEximDto>();
            while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                var zipEntryName = zipEntry.getName();
                
                // Validate zip entry path to prevent path traversal attacks
                try {
                    PathUtils.validateZipEntryPath(zipEntryName);
                } catch (IllegalArgumentException e) {
                    log.warn("Skipping zip entry with invalid path: {}", zipEntryName, e);
                    continue;
                }
                
                if (zipEntryName.startsWith(PROMPTS_FOLDER) && zipEntryName.endsWith(JSON_FILE_EXTENSION)) {
                    promptsEximDtos.put(zipEntryName, jsonMapper.readValue(zipInputStream, PromptsEximDto.class));
                } else {
                    log.info("Ignoring file {} in zip archive during import. Prompt import context {}",
                            zipEntryName, importPrompts);
                }
            }

            var compacted = compactPromptsEximDtos(promptsEximDtos);
            return promptEximService.importPrompts(importPrompts, compacted);
        } catch (Exception ex) {
            log.debug("Prompt file {} import failed", rootPath, ex);
            return ImportResourcesFileResult.builder()
                    .importResults(List.of())
                    .error(ex.getMessage())
                    .build();
        }
    }

    public ImportResourcesPreview previewImportPromptsFromZip(ImportResources importPrompts, MultipartFile zipFile) {
        try (ZipInputStream zipInputStream = new ZipInputStream(zipFile.getInputStream())) {

            ZipEntry zipEntry;
            List<ImportResourcePreview> previews = new ArrayList<>();
            while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                var zipEntryName = zipEntry.getName();
                
                // Validate zip entry path to prevent path traversal attacks
                try {
                    PathUtils.validateZipEntryPath(zipEntryName);
                } catch (IllegalArgumentException e) {
                    log.warn("Skipping zip entry with invalid path: {}", zipEntryName, e);
                    continue;
                }
                
                if (zipEntryName.startsWith(PROMPTS_FOLDER) && zipEntryName.endsWith(JSON_FILE_EXTENSION)) {
                    var promptsEximDto = jsonMapper.readValue(zipInputStream, PromptsEximDto.class);
                    promptsEximDto.getPrompts().stream()
                            .map(prompt -> getPromptPathParts(importPrompts, prompt))
                            .map(pathParts -> buildImportResourcePreview(pathParts, zipEntryName))
                            .forEach(previews::add);
                } else {
                    log.info("Ignoring file {} in zip archive during import preview", zipEntryName);
                }
            }

            return ImportResourcesPreview.builder()
                    .resourcePreviews(previews)
                    .build();
        } catch (Exception ex) {
            log.debug("Prompt file {} import preview failed", zipFile.getOriginalFilename(), ex);
            throw new IllegalArgumentException(ex);
        }
    }


    private PromptsEximDto compactPromptsEximDtos(HashMap<String, PromptsEximDto> fileNameToPromptsEximDtos) {
        checkPromptsExistence(fileNameToPromptsEximDtos);
        checkPromptConflicts(fileNameToPromptsEximDtos);

        var compactedPrompts = fileNameToPromptsEximDtos.values().stream()
                .map(PromptsEximDto::getPrompts)
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .collect(Collectors.toList());

        var compactedFolders = fileNameToPromptsEximDtos.values().stream()
                .map(PromptsEximDto::getFolders)
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .collect(Collectors.toMap(folderDto -> PathUtils.trimTrailingSlash(folderDto.getId()), Function.identity(), (existing, replacement) -> replacement))
                .values();

        return PromptsEximDto.builder()
                .prompts(compactedPrompts)
                .folders(new ArrayList<>(compactedFolders))
                .build();
    }

    private void checkPromptsExistence(HashMap<String, PromptsEximDto> fileNameToPromptsEximDtos) {
        if (fileNameToPromptsEximDtos.isEmpty()) {
            throw new IllegalArgumentException("No prompt files (e.g., `prompts/*.json`) found or loaded from the archive. "
                    + "Please ensure prompt files are placed in a `prompts/` directory and have a `.json` extension.");
        }

        var noPrompts = fileNameToPromptsEximDtos.values().stream()
                .map(PromptsEximDto::getPrompts)
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .findAny()
                .isEmpty();
        if (noPrompts) {
            throw new IllegalArgumentException("Prompt files (e.g., `prompts/*.json`) were found in the archive, "
                    + "but they do not contain prompts. Please verify the content of these files.");
        }
    }

    private void checkPromptConflicts(HashMap<String, PromptsEximDto> fileNameToPromptsEximDtos) {
        var duplicatesWithinFiles = findSamePromptsWithinSameFiles(fileNameToPromptsEximDtos);
        var duplicatesAcrossFiles = findSamePromptsWithinDifferentFiles(fileNameToPromptsEximDtos);

        var hasConflicts = !duplicatesWithinFiles.isEmpty() || !duplicatesAcrossFiles.isEmpty();

        if (hasConflicts) {
            var errorMessage = new StringBuilder("Prompt ID uniqueness violation. Conflicts found:");

            if (!duplicatesWithinFiles.isEmpty()) {
                errorMessage.append("\n  Prompts duplicated within the same file:\n");
                duplicatesWithinFiles.forEach((filename, ids) ->
                        errorMessage.append(String.format("    - File '%s' has duplicate prompt IDs: %s", filename, ids))
                );
            }

            if (!duplicatesAcrossFiles.isEmpty()) {
                errorMessage.append("\n  Prompts shared across different files:\n");
                duplicatesAcrossFiles.forEach((promptId, filenames) ->
                        errorMessage.append(String.format("    - Prompt ID '%s' is found in multiple files: %s", promptId, filenames))
                );
            }
            throw new IllegalArgumentException(errorMessage.toString().trim());
        }
    }

    /**
     * Finds prompt IDs duplicated within the same file.
     *
     * @param promptsEximDtos Map of filename to {@code PromptsEximDto} (containing prompt lists).
     * @return A map of filename to a Set of prompt IDs that appear more than once
     *     <em>within that specific file</em>. Only files with such duplicates are included.
     */
    private Map<String, Set<String>> findSamePromptsWithinSameFiles(Map<String, PromptsEximDto> promptsEximDtos) {
        var filesWithDuplicatePrompts = new HashMap<String, Set<String>>();

        for (var entry : promptsEximDtos.entrySet()) {
            var filename = entry.getKey();
            var dto = entry.getValue();

            if (dto.getPrompts() == null) {
                continue;
            }

            var promptIdsInFile = dto.getPrompts().stream()
                    .map(prompt -> PathUtils.trimTrailingSlash(prompt.getId()))
                    .toList();

            var idCounts = promptIdsInFile.stream()
                    .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

            var duplicateIdsInThisFile = idCounts.entrySet().stream()
                    .filter(countEntry -> countEntry.getValue() > 1)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toSet());

            if (!duplicateIdsInThisFile.isEmpty()) {
                filesWithDuplicatePrompts.put(filename, duplicateIdsInThisFile);
            }
        }

        return filesWithDuplicatePrompts;
    }

    /**
     * Finds prompt IDs that are present in multiple different files.
     *
     * @param promptsEximDtos Map of filename to {@code PromptsEximDto} (containing prompt lists).
     * @return A map of a shared prompt ID to a Set of filenames where it appears.
     *     Only prompt IDs found in <em>more than one file</em> are included.
     */
    private Map<String, Set<String>> findSamePromptsWithinDifferentFiles(Map<String, PromptsEximDto> promptsEximDtos) {
        var promptToFilenamesMap = new HashMap<String, Set<String>>();

        for (var entry : promptsEximDtos.entrySet()) {
            var filename = entry.getKey();
            var dto = entry.getValue();

            if (dto.getPrompts() == null) {
                continue;
            }

            for (var prompt : dto.getPrompts()) {
                var id = PathUtils.trimTrailingSlash(prompt.getId());
                promptToFilenamesMap
                        .computeIfAbsent(id, k -> new HashSet<>())
                        .add(filename);
            }
        }

        return promptToFilenamesMap.entrySet().stream()
                .filter(entry -> entry.getValue().size() > 1)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private PathUtils.VersionedPathParts getPromptPathParts(ImportResources importPrompts, PromptEximDto prompt) {
        var rootPath = importPrompts.getPath();
        var rootPathStripped = StringUtils.stripEnd(rootPath, "/");

        var rawPath = prompt.getId();
        var sourcePath = StringUtils.removeStart(rawPath, PROMPTS_FOLDER);
        var sourcePathWithoutPublic = StringUtils.removeStart(sourcePath, PUBLIC_FOLDER);

        var targetPath = rootPathStripped + "/" + sourcePathWithoutPublic;

        return PathUtils.parseVersionedPath(targetPath);
    }

    private ImportResourcePreview buildImportResourcePreview(PathUtils.VersionedPathParts promptPathParts, String fileName) {
        return ImportResourcePreview.builder()
                .name(promptPathParts.getName())
                .version(promptPathParts.getVersion())
                .fileName(fileName)
                .build();
    }

}
