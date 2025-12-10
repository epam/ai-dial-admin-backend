package com.epam.aidial.cfg.service;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.dto.ApplicationEximDto;
import com.epam.aidial.cfg.dto.ApplicationsEximDto;
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
public class ZipApplicationEximService {

    private static final String APPLICATIONS_FOLDER = "applications/";
    private static final String PUBLIC_FOLDER = "public/";
    private static final String APPLICATIONS_FILENAME = "applications.json";
    private static final String JSON_FILE_EXTENSION = ".json";
    private static final String APPLICATIONS_FULL_PATH = APPLICATIONS_FOLDER + APPLICATIONS_FILENAME;

    private final ResourceImportValidator uniquenessValidator;

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
                log.warn("An error occurred while exporting applications", e);
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
                    log.warn("Ignoring file {} in zip archive during import. Application import context {}",
                            zipEntryName, importApplications);
                }
            }

            var compacted = compactApplicationsEximDtos(importApplications, applicationsEximDtos);
            return applicationEximService.importApplications(importApplications, compacted);
        } catch (Exception ex) {
            log.warn("Application file {} import failed", rootPath, ex);
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
            log.warn("Application file {} import preview failed", zipFile.getOriginalFilename(), ex);
            throw new ImportPreviewException(String.format("Application file '%s' import preview failed", zipFile.getOriginalFilename()));
        }
    }

    private ApplicationsEximDto compactApplicationsEximDtos(ImportResources importApplications, HashMap<String, ApplicationsEximDto> fileNameToApplicationsEximDtos) {
        uniquenessValidator.checkApplicationConflicts(importApplications, fileNameToApplicationsEximDtos);

        var compactedApplications = fileNameToApplicationsEximDtos.values().stream()
                .map(ApplicationsEximDto::getApplications)
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .collect(Collectors.toList());

        return ApplicationsEximDto.builder()
                .applications(compactedApplications)
                .build();
    }

    private PathUtils.VersionedPathParts getApplicationPathParts(ImportResources importApplications, ApplicationEximDto application) {
        var rootPath = importApplications.getPath();
        var rootPathStripped = StringUtils.stripEnd(rootPath, "/");

        var folderPathWithoutPublic = StringUtils.removeStart(application.getFolderId(), PUBLIC_FOLDER);
        var applicationName = EximServiceHelper.getVersionedName(application);
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
    
}