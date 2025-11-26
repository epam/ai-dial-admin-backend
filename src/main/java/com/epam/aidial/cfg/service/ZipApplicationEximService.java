package com.epam.aidial.cfg.service;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.dto.ApplicationEximDto;
import com.epam.aidial.cfg.dto.ApplicationsEximDto;
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
public class ZipApplicationEximService {

    private static final String APPLICATIONS_FOLDER = "applications/";
    private static final String PUBLIC_FOLDER = "public/";
    private static final String APPLICATIONS_FILENAME = "applications.json";
    private static final String JSON_FILE_EXTENSION = ".json";
    private static final String APPLICATIONS_FULL_PATH = APPLICATIONS_FOLDER + APPLICATIONS_FILENAME;

    private final JsonMapper jsonMapper = JsonMapper.builder()
            .configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, false)
            .build();

    private final ApplicationEximService applicationEximService;

    public StreamingResponseBody exportApplications(List<String> paths) {
        var token = AuthorizationTokenHolder.getToken();

        return outputStream -> {
            try (
                    var ignored = new AuthorizationTokenWrapper(token);
                    var zos = new ZipOutputStream(outputStream)
            ) {
                var applicationsExim = applicationEximService.exportApplications(paths);
                zos.putNextEntry(new ZipEntry(APPLICATIONS_FULL_PATH));
                zos.write(jsonMapper.writeValueAsString(applicationsExim).getBytes());
                zos.closeEntry();
            } catch (Exception e) {
                log.error("An error occurred while exporting applications", e);
                throw e;
            }
        };
    }

    public ImportResourcesFileResult importApplications(ImportResources importApplications, MultipartFile zipFile) throws IOException {
        var rootPath = importApplications.getPath();
        var inputStream = zipFile.getInputStream();

        try (ZipInputStream zipInputStream = new ZipInputStream(inputStream)) {

            ZipEntry zipEntry;
            var applicationsEximDtos = new HashMap<String, ApplicationsEximDto>();
            while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                var zipEntryName = zipEntry.getName();

                // Validate zip entry path to prevent path traversal attacks
                try {
                    zipEntryName = PathUtils.validateZipEntryPath(zipEntryName);
                } catch (IllegalArgumentException e) {
                    log.warn("Skipping zip entry with invalid path: {}", zipEntryName, e);
                    continue;
                }

                if (zipEntryName.startsWith(APPLICATIONS_FOLDER) && zipEntryName.endsWith(JSON_FILE_EXTENSION)) {
                    applicationsEximDtos.put(zipEntryName, jsonMapper.readValue(zipInputStream, ApplicationsEximDto.class));
                } else {
                    log.info("Ignoring file {} in zip archive during import. Application import context {}",
                            zipEntryName, importApplications);
                }
            }

            var compacted = compactApplicationsEximDtos(applicationsEximDtos);
            return applicationEximService.importApplications(importApplications, compacted);
        } catch (Exception ex) {
            log.debug("Application file {} import failed", rootPath, ex);
            return ImportResourcesFileResult.builder()
                    .importResults(List.of())
                    .error(ex.getMessage())
                    .build();
        }
    }

    public ImportResourcesPreview previewImportApplicationsFromZip(ImportResources importApplications, MultipartFile zipFile) {
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

                if (zipEntryName.startsWith(APPLICATIONS_FOLDER) && zipEntryName.endsWith(JSON_FILE_EXTENSION)) {
                    var applicationsEximDto = jsonMapper.readValue(zipInputStream, ApplicationsEximDto.class);
                    String finalZipEntryName = zipEntryName;
                    applicationsEximDto.getApplications().stream()
                            .map(applicationEximDto -> getApplicationPathParts(importApplications, applicationEximDto))
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
            log.debug("Application file {} import preview failed", zipFile.getOriginalFilename(), ex);
            throw new IllegalArgumentException(ex);
        }
    }

    private ApplicationsEximDto compactApplicationsEximDtos(HashMap<String, ApplicationsEximDto> fileNameToApplicationsEximDtos) {
        checkApplicationsExistence(fileNameToApplicationsEximDtos);
        checkApplicationConflicts(fileNameToApplicationsEximDtos);

        var compactedApplications = fileNameToApplicationsEximDtos.values().stream()
                .map(ApplicationsEximDto::getApplications)
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .collect(Collectors.toList());

        var compactedFolders = fileNameToApplicationsEximDtos.values().stream()
                .map(ApplicationsEximDto::getFolders)
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .collect(Collectors.toMap(folderDto -> PathUtils.trimTrailingSlash(folderDto.getId()), Function.identity(), (existing, replacement) -> replacement))
                .values();

        return ApplicationsEximDto.builder()
                .applications(compactedApplications)
                .folders(new ArrayList<>(compactedFolders))
                .build();
    }

    private void checkApplicationsExistence(HashMap<String, ApplicationsEximDto> fileNameToApplicationsEximDtos) {
        if (fileNameToApplicationsEximDtos.isEmpty()) {
            throw new IllegalArgumentException("No application files (e.g., `applications/*.json`) found or loaded from the archive. "
                    + "Please ensure application files are placed in a `applications/` directory and have a `.json` extension.");
        }

        var noApplications = fileNameToApplicationsEximDtos.values().stream()
                .map(ApplicationsEximDto::getApplications)
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .findAny()
                .isEmpty();
        if (noApplications) {
            throw new IllegalArgumentException("Application files (e.g., `applications/*.json`) were found in the archive, "
                    + "but they do not contain applications. Please verify the content of these files.");
        }
    }

    private void checkApplicationConflicts(HashMap<String, ApplicationsEximDto> fileNameToApplicationsEximDtos) {
        var duplicatesWithinFiles = findSameApplicationsWithinSameFiles(fileNameToApplicationsEximDtos);
        var duplicatesAcrossFiles = findSameApplicationsWithinDifferentFiles(fileNameToApplicationsEximDtos);

        var hasConflicts = !duplicatesWithinFiles.isEmpty() || !duplicatesAcrossFiles.isEmpty();

        if (hasConflicts) {
            var errorMessage = new StringBuilder("Application ID uniqueness violation. Conflicts found:");

            if (!duplicatesWithinFiles.isEmpty()) {
                errorMessage.append("\n  Applications duplicated within the same file:\n");
                duplicatesWithinFiles.forEach((filename, ids) ->
                        errorMessage.append(String.format("    - File '%s' has duplicate application IDs: %s", filename, ids))
                );
            }

            if (!duplicatesAcrossFiles.isEmpty()) {
                errorMessage.append("\n  Applications shared across different files:\n");
                duplicatesAcrossFiles.forEach((applicationId, filenames) ->
                        errorMessage.append(String.format("    - Application ID '%s' is found in multiple files: %s", applicationId, filenames))
                );
            }
            throw new IllegalArgumentException(errorMessage.toString().trim());
        }
    }

    /**
     * Finds application IDs duplicated within the same file.
     *
     * @param applicationsEximDtos Map of filename to {@code ApplicationsEximDto} (containing application lists).
     * @return A map of filename to a Set of application IDs that appear more than once
     *     <em>within that specific file</em>. Only files with such duplicates are included.
     */
    private Map<String, Set<String>> findSameApplicationsWithinSameFiles(Map<String, ApplicationsEximDto> applicationsEximDtos) {
        var filesWithDuplicateApplications = new HashMap<String, Set<String>>();

        for (var entry : applicationsEximDtos.entrySet()) {
            var filename = entry.getKey();
            var dto = entry.getValue();

            if (dto.getApplications() == null) {
                continue;
            }

            var applicationIdsInFile = dto.getApplications().stream()
                    .map(application -> PathUtils.trimTrailingSlash(application.getApplicationTypeSchemaId()))
                    .toList();

            var idCounts = applicationIdsInFile.stream()
                    .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

            var duplicateIdsInThisFile = idCounts.entrySet().stream()
                    .filter(countEntry -> countEntry.getValue() > 1)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toSet());

            if (!duplicateIdsInThisFile.isEmpty()) {
                filesWithDuplicateApplications.put(filename, duplicateIdsInThisFile);
            }
        }

        return filesWithDuplicateApplications;
    }

    /**
     * Finds application IDs that are present in multiple different files.
     *
     * @param applicationsEximDtos Map of filename to {@code ApplicationsEximDto} (containing application lists).
     * @return A map of a shared application ID to a Set of filenames where it appears.
     *     Only application IDs found in <em>more than one file</em> are included.
     */
    private Map<String, Set<String>> findSameApplicationsWithinDifferentFiles(Map<String, ApplicationsEximDto> applicationsEximDtos) {
        var applicationToFilenamesMap = new HashMap<String, Set<String>>();

        for (var entry : applicationsEximDtos.entrySet()) {
            var filename = entry.getKey();
            var dto = entry.getValue();

            if (dto.getApplications() == null) {
                continue;
            }

            for (var application : dto.getApplications()) {
                var id = PathUtils.trimTrailingSlash(application.getApplicationTypeSchemaId());
                applicationToFilenamesMap
                        .computeIfAbsent(id, k -> new HashSet<>())
                        .add(filename);
            }
        }

        return applicationToFilenamesMap.entrySet().stream()
                .filter(entry -> entry.getValue().size() > 1)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private PathUtils.VersionedPathParts getApplicationPathParts(ImportResources importApplications, ApplicationEximDto application) {
        var rootPath = importApplications.getPath();
        var rootPathStripped = StringUtils.stripEnd(rootPath, "/");

        var folderPathWithoutPublic = StringUtils.removeStart(application.getFolderId(), PUBLIC_FOLDER);
        var applicationName = getVersionedName(application);
        var targetPath = rootPathStripped + "/" + folderPathWithoutPublic + applicationName;

        return PathUtils.parseVersionedPath(targetPath);
    }

    private ImportResourcePreview buildImportResourcePreview(PathUtils.VersionedPathParts applicationPathParts, String fileName) {
        return ImportResourcePreview.builder()
                .name(applicationPathParts.getName())
                .version(applicationPathParts.getVersion())
                .fileName(fileName)
                .build();
    }

    public String getVersionedName(ApplicationEximDto applicationEximDto) {
        return applicationEximDto.getDisplayVersion() == null
                ? applicationEximDto.getName()
                : applicationEximDto.getName() + "__" + applicationEximDto.getDisplayVersion();
    }

}