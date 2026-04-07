package com.epam.aidial.cfg.service;

import com.epam.aidial.cfg.client.FileClient;
import com.epam.aidial.cfg.client.ResourceClient;
import com.epam.aidial.cfg.client.dto.FileMetadataDto;
import com.epam.aidial.cfg.client.mapper.FileClientMapper;
import com.epam.aidial.cfg.client.mapper.FolderMapper;
import com.epam.aidial.cfg.client.mapper.ResourceClientMapper;
import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.domain.model.activity.ActivityResourceType;
import com.epam.aidial.cfg.domain.model.activity.ActivityType;
import com.epam.aidial.cfg.domain.service.AuditActivityLogService;
import com.epam.aidial.cfg.exception.ResourceNotFoundException;
import com.epam.aidial.cfg.model.ExportResource;
import com.epam.aidial.cfg.model.FileNodeInfo;
import com.epam.aidial.cfg.model.FolderInfo;
import com.epam.aidial.cfg.model.ImportConflictResolutionStrategy;
import com.epam.aidial.cfg.model.ImportResources;
import com.epam.aidial.cfg.model.ImportResourcesFileResult;
import com.epam.aidial.cfg.model.ImportResourcesResult;
import com.epam.aidial.cfg.model.MoveResource;
import com.epam.aidial.cfg.model.NodeType;
import com.epam.aidial.cfg.model.ResourceMetadataRequest;
import com.epam.aidial.cfg.model.ResourceType;
import com.epam.aidial.cfg.security.AuthorizationTokenHolder;
import com.epam.aidial.cfg.security.AuthorizationTokenWrapper;
import com.epam.aidial.cfg.utils.PathUtils;
import feign.FeignException;
import feign.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.InputStream;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import static com.epam.aidial.cfg.client.mapper.FileClientMapper.FILES_PREFIX;

@Service
@RequiredArgsConstructor
@LogExecution
@Slf4j
public class FileService implements ResourceService {
    public static final String DIAL_FOLDER_FILE = ".dial_folder";

    private final FileClient fileClient;
    private final FileClientMapper fileClientMapper;
    private final ResourceClient resourceClient;
    private final ResourceClientMapper resourceClientMapper;
    private final FolderMapper folderMapper;
    private final ResourceImportValidator uniquenessValidator;
    private final AuditActivityLogService auditActivityLogService;

    @Value("${files.import.consecutiveErrorsThreshold}")
    private int importErrorsThreshold;

    public FileNodeInfo getAll(ResourceMetadataRequest request) {
        var filesMetadataResponse = getMetadata(request);
        return fileClientMapper.toFileInfo(filesMetadataResponse);
    }

    @Override
    public FolderInfo getFolders(ResourceMetadataRequest request) {
        try {
            var filesMetadataResponse = getMetadata(request);
            return folderMapper.toFolderInfo(filesMetadataResponse, FILES_PREFIX);
        } catch (ResourceNotFoundException notFound) {
            return null;
        }
    }

    @Override
    public FileMetadataDto getMetadata(ResourceMetadataRequest request) {
        return fileClient.getFilesMetadata(request.getPath(), request.isRecursive(), request.getNextToken(), request.isPermissions());
    }

    public Response get(String path) {
        return fileClient.getFile(path);
    }

    public ImportResourcesFileResult uploadFile(List<MultipartFile> files, ImportResources importFile) {
        var path = importFile.getPath();
        var fileNames = files.stream()
                .map(MultipartFile::getOriginalFilename)
                .collect(Collectors.joining(","));
        ImportResourcesFileResult uploadResult = null;
        try {
            var strategy = importFile.getConflictResolutionStrategy();
            var circuitBreaker = new SimpleCircuitBreaker(importErrorsThreshold);
            var results = new ArrayList<ImportResourcesResult>();
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    var targetPath = path + file.getOriginalFilename();
                    var result = createFileWithCircuitBreaker(file, null, targetPath, strategy, circuitBreaker);
                    results.add(result);
                    log.debug("File {} was successfully imported", targetPath);
                }
            }
            uploadResult = ImportResourcesFileResult.builder()
                    .importResults(results)
                    .build();
            return uploadResult;
        } catch (Exception ex) {
            log.warn("Files {} import failed", fileNames, ex);
            String errorMessage = StringUtils.isEmpty(ex.getMessage())
                    ? "An unknown error occurred during files import"
                    : ex.getMessage();
            uploadResult = ImportResourcesFileResult.builder()
                    .importResults(List.of())
                    .error(errorMessage)
                    .build();
            return uploadResult;
        } finally {
            auditActivityLogService.logFileUpload(ActivityType.FileUpload, importFile, null, fileNames, uploadResult);
        }
    }

    private Map<String, String> getUploadHeader(ImportConflictResolutionStrategy strategy) {
        return switch (strategy) {
            case SKIP -> Map.of("If-None-Match", "*");
            case OVERRIDE -> Map.of("If-Match", "*");
        };
    }

    public ImportResourcesFileResult uploadFileZip(ImportResources importFiles, MultipartFile zipFile) {
        String fileName = zipFile == null ? "not specified" : zipFile.getOriginalFilename();
        ImportResourcesFileResult uploadResult = null;
        try {
            try {
                uniquenessValidator.validateFileImportInZip(importFiles, zipFile);
            } catch (Exception ex) {
                log.warn("Zip validation failed for file {}: {}", fileName, ex);
                uploadResult = ImportResourcesFileResult.builder()
                        .importResults(List.of())
                        .error(ex.getMessage())
                        .build();

                return uploadResult;
            }
            try {
                var rootPath = importFiles.getPath();
                var rootPathStripped = StringUtils.stripEnd(rootPath, "/");
                var inputStream = zipFile.getInputStream();
                var circuitBreaker = new SimpleCircuitBreaker(importErrorsThreshold);

                var results = new ArrayList<ImportResourcesResult>();
                try (ZipInputStream zipInputStream = new ZipInputStream(inputStream)) {
                    ZipEntry zipEntry;
                    while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                        var result = importZipFile(rootPathStripped, zipEntry, zipInputStream, importFiles,
                                circuitBreaker);
                        results.add(result);
                    }
                }
                uploadResult = ImportResourcesFileResult.builder()
                        .importResults(results)
                        .build();
                return uploadResult;
            } catch (Exception ex) {
                log.warn("File {} import failed", fileName, ex);
                String errorMessage = StringUtils.isEmpty(ex.getMessage())
                        ? "An unknown error occurred during file import"
                        : ex.getMessage();
                uploadResult = ImportResourcesFileResult.builder()
                        .importResults(List.of())
                        .error(errorMessage)
                        .build();
                return uploadResult;
            }
        } finally {
            auditActivityLogService.logFileUpload(ActivityType.FileUploadZip, importFiles, fileName, null, uploadResult);
        }
    }

    private ImportResourcesResult importZipFile(String rootPath,
                                                ZipEntry zipEntry,
                                                InputStream fileInputStream,
                                                ImportResources importFiles,
                                                SimpleCircuitBreaker circuitBreaker) {
        String sourcePath = null;
        String targetPath = null;
        try {
            var filename = zipEntry.getName();

            // Validate zip entry path to prevent path traversal attacks
            try {
                filename = PathUtils.validateZipEntryPath(filename);
            } catch (IllegalArgumentException e) {
                log.warn("Skipping zip entry with invalid path: {}", filename, e);
                return ImportResourcesResult.createFailure(filename, null,
                        "Invalid zip entry path: " + e.getMessage());
            }

            sourcePath = StringUtils.removeStart(filename, "files/");
            if (importFiles.isFlatImport()) {
                var fileNameWithoutPath = PathUtils.parsePath(filename).getName();
                targetPath = rootPath + "/" + fileNameWithoutPath;
            } else {
                var sourcePathWithoutPublic = StringUtils.removeStart(sourcePath, "public/");
                targetPath = rootPath + "/" + sourcePathWithoutPublic;
            }
            byte[] fileData = fileInputStream.readAllBytes();

            String contentTypeFromName = URLConnection.guessContentTypeFromName(filename);
            MultipartFile extractedFile = new MockMultipartFile("file", filename, contentTypeFromName, fileData);
            var result = createFileWithCircuitBreaker(extractedFile, sourcePath, targetPath, importFiles.getConflictResolutionStrategy(), circuitBreaker);
            log.debug("File {} was successfully imported", sourcePath);
            return result;
        } catch (Exception ex) {
            return ImportResourcesResult.createFailure(sourcePath, targetPath, ex.getMessage());
        }
    }

    private ImportResourcesResult createFileWithCircuitBreaker(MultipartFile file,
                                                               String sourcePath,
                                                               String targetPath,
                                                               ImportConflictResolutionStrategy conflictResolutionStrategy,
                                                               SimpleCircuitBreaker circuitBreaker) {
        return circuitBreaker.apply(
                () -> createFileOrThrow(file, sourcePath, targetPath, conflictResolutionStrategy),
                (ex) -> {
                    log.warn("File {} import failed", targetPath, ex);
                    return ImportResourcesResult.createFailure(sourcePath, targetPath, ex.getMessage());
                },
                () -> {
                    log.warn("File {} import was skipped due to consecutive errors", targetPath);
                    return ImportResourcesResult.createFailure(sourcePath, targetPath, "Skipped due to consecutive errors");
                }
        );
    }

    private ImportResourcesResult createFileOrThrow(MultipartFile file,
                                                    String sourcePath,
                                                    String targetPath,
                                                    ImportConflictResolutionStrategy conflictResolutionStrategy) {
        try {
            var header = getUploadHeader(conflictResolutionStrategy);
            fileClient.uploadFile(file, targetPath, header);
            return ImportResourcesResult.createSuccess(sourcePath, targetPath);
        } catch (Exception ex) {
            if (ex instanceof FeignException feignException) {
                if (feignException.status() == 412) {
                    log.debug("File {} import skipped - file already exists", targetPath, ex);
                    return ImportResourcesResult.createAlreadyExists(sourcePath, targetPath);
                }
            }
            throw ex;
        }
    }

    public void deleteFile(String path) {
        delete(path, null);
    }

    @Override
    public void delete(String path, String etag) {
        fileClient.deleteFile(path);
        auditActivityLogService.logAssetChange(ActivityType.Delete, ActivityResourceType.File, path);
    }

    public void deleteFiles(List<String> paths) {
        List<String> deletedFiles = new ArrayList<>();
        for (var path : paths) {
            try {
                deleteFile(path);
                deletedFiles.add(path);
            } catch (Exception exception) {
                log.warn("Unable to delete file: {}, deleted files: {}", path, deletedFiles, exception);
                throw exception;
            }
        }
    }

    @Override
    public void move(MoveResource moveResource) {
        var moveResourceDto = resourceClientMapper.toMoveResourceDto(moveResource, FILES_PREFIX);
        resourceClient.move(moveResourceDto);
    }

    @Override
    public ResourceType getResourceType() {
        return ResourceType.FILE;
    }

    public StreamingResponseBody export(ExportResource exportResource) {
        var exportEntries = resolveExportFileEntries(exportResource);
        var sortedPaths = exportEntries.keySet().stream()
                .sorted()
                .toList();

        var token = AuthorizationTokenHolder.getToken();
        return outputStream -> {
            try (
                    var ignored = new AuthorizationTokenWrapper(token);
                    var zos = new ZipOutputStream(outputStream)
            ) {

                for (var path : sortedPaths) {
                    var fileResponse = get(path);
                    zos.putNextEntry(new ZipEntry("files/" + exportEntries.get(path)));
                    try (InputStream responseBodyStream = fileResponse.body().asInputStream()) {
                        byte[] buffer = new byte[1024];
                        int bytesRead = responseBodyStream.read(buffer);
                        while (bytesRead != -1) {
                            zos.write(buffer, 0, bytesRead);
                            bytesRead = responseBodyStream.read(buffer);
                        }
                    }
                    zos.closeEntry();
                }
            } catch (Exception e) {
                log.warn("An error occurred while exporting files", e);
                throw e;
            }
        };
    }

    public boolean fileExists(String path) {
        try {
            var request = ResourceMetadataRequest.builder().path(path).build();
            var filesNode = getAll(request);
            return filesNode.getNodeType() == NodeType.ITEM;
        } catch (ResourceNotFoundException e) {
            return false;
        }
    }

    private Map<String, String> resolveExportFileEntries(ExportResource exportResource) {
        Map<String, String> entries = new HashMap<>();
        for (String path : exportResource.getPaths()) {
            if (PathUtils.isFolderPath(path)) {
                addFolderExportEntries(entries, path);
            } else {
                addSingleFileExportEntry(entries, path);
            }
        }
        return entries;
    }

    private void addFolderExportEntries(Map<String, String> entries, String path) {
        var folderName = PathUtils.folderNameWithoutPath(path);
        var archiveFolderPath = "public/" + folderName;
        for (String filePath : collectFilePathsByPath(path)) {
            var pathParts = PathUtils.parsePath(filePath);
            var fileName = pathParts.getName();
            var insideFolder = pathParts.getFolderId().substring(path.length());
            if (entries.containsKey(filePath)) {
                throw new IllegalStateException("Duplicate entry for path: " + filePath);
            }
            entries.put(filePath, archiveFolderPath + insideFolder + fileName);
        }
    }

    private void addSingleFileExportEntry(Map<String, String> entries, String filePath) {
        var fileName = PathUtils.parsePath(filePath).getName();
        if (isNotTechFile(filePath)) {
            if (entries.containsKey(filePath)) {
                throw new IllegalStateException("Duplicate entry for path: " + filePath);
            }
            entries.putIfAbsent(filePath, "public/" + fileName);
        }
    }

    private Set<String> collectFilePathsByPath(String path) {
        if (!PathUtils.isFolderPath(path)) {
            return path != null && !path.isEmpty() ? Set.of(path) : Collections.emptySet();
        }
        try {
            var request = ResourceMetadataRequest.builder()
                    .path(path)
                    .recursive(true)
                    .build();
            FileNodeInfo node = getAll(request);
            return collectPaths(node);
        } catch (ResourceNotFoundException e) {
            log.debug("Path not found for export: {}", path, e);
            return Collections.emptySet();
        }
    }

    private Set<String> collectPaths(FileNodeInfo node) {
        if (node == null) {
            return Collections.emptySet();
        }
        if (node.getNodeType() == NodeType.ITEM) {
            return node.getPath() != null && isNotTechFile(node.getPath()) ? Set.of(node.getPath()) : Collections.emptySet();
        }
        if (node.getNodeType() == NodeType.FOLDER && node.getItems() != null) {
            return node.getItems().stream().filter(i -> i.getNodeType() == NodeType.ITEM)
                    .map(FileNodeInfo::getPath)
                    .filter(this::isNotTechFile)
                    .collect(Collectors.toSet());
        }
        return Collections.emptySet();
    }

    private boolean isNotTechFile(String path) {
        var fileName = PathUtils.parsePath(path).getName();
        return !DIAL_FOLDER_FILE.equals(fileName);
    }
}