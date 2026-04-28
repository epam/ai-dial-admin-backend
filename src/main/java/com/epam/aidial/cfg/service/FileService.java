package com.epam.aidial.cfg.service;

import com.epam.aidial.cfg.client.FileClient;
import com.epam.aidial.cfg.client.ResourceClient;
import com.epam.aidial.cfg.client.dto.FileMetadataDto;
import com.epam.aidial.cfg.client.mapper.FileClientMapper;
import com.epam.aidial.cfg.client.mapper.FolderMapper;
import com.epam.aidial.cfg.client.mapper.ResourceClientMapper;
import com.epam.aidial.cfg.configuration.logging.LogExecution;
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
import com.epam.aidial.cfg.utils.ExportPathUtils;
import com.epam.aidial.cfg.utils.HeaderUtils;
import com.epam.aidial.cfg.utils.PathUtils;
import com.epam.aidial.cfg.utils.ResourceEximExportHelper;
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
import java.util.List;
import java.util.Map;
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

    private static final String INVALID_EXPORT_ZIP =
            "Invalid archive format. Please upload a valid aidial-admin archive.";

    private final FileClient fileClient;
    private final FileClientMapper fileClientMapper;
    private final ResourceClient resourceClient;
    private final ResourceClientMapper resourceClientMapper;
    private final FolderMapper folderMapper;
    private final ResourceImportValidator uniquenessValidator;

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
            return ImportResourcesFileResult.builder()
                    .importResults(results)
                    .build();
        } catch (Exception ex) {
            String fileNames = files.stream()
                    .map(MultipartFile::getOriginalFilename)
                    .collect(Collectors.joining(","));
            log.warn("Files {} import failed", fileNames, ex);
            String errorMessage = StringUtils.isEmpty(ex.getMessage())
                    ? "An unknown error occurred during files import"
                    : ex.getMessage();
            return ImportResourcesFileResult.builder()
                    .importResults(List.of())
                    .error(errorMessage)
                    .build();
        }
    }

    public ImportResourcesFileResult uploadFileZip(ImportResources importFiles, MultipartFile zipFile) {
        String fileName = zipFile == null ? "not specified" : zipFile.getOriginalFilename();
        try {
            uniquenessValidator.validateFileImportInZip(importFiles, zipFile);
        } catch (Exception ex) {
            log.warn("Zip validation failed for file {}: {}", fileName, ex);
            return ImportResourcesFileResult.builder()
                    .importResults(List.of())
                    .error(INVALID_EXPORT_ZIP)
                    .build();
        }
        try (ZipInputStream zipInputStream = new ZipInputStream(zipFile.getInputStream())) {
            var rootPath = importFiles.getPath();
            var rootPathStripped = StringUtils.stripEnd(rootPath, "/");
            var circuitBreaker = new SimpleCircuitBreaker(importErrorsThreshold);
            ZipEntry zipEntry;
            var results = new ArrayList<ImportResourcesResult>();
            boolean hasValidEntries = false;
            while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                var filename = zipEntry.getName();
                try {
                    filename = PathUtils.validateZipEntryPath(filename);
                } catch (IllegalArgumentException e) {
                    log.warn("Skipping zip entry with invalid path: {}", filename, e);
                    results.add(ImportResourcesResult.createFailure(filename, null,
                            "Invalid zip entry path: " + e.getMessage()));
                    continue;
                }
                if (!filename.startsWith(FILES_PREFIX)) {
                    log.warn("Ignoring file {} in zip archive during import.", filename);
                    results.add(ImportResourcesResult.createFailure(filename, null,
                            "Invalid zip entry path: " + filename));
                    continue;
                }
                hasValidEntries = true;
                var result = importZipFile(rootPathStripped, filename, zipInputStream, importFiles,
                        circuitBreaker);
                results.add(result);
            }
            if (!hasValidEntries) {
                log.warn("No valid file entries found in zip. path={}", rootPath);
                return ImportResourcesFileResult.builder()
                        .importResults(List.of())
                        .error(INVALID_EXPORT_ZIP)
                        .build();
            }
            return ImportResourcesFileResult.builder()
                    .importResults(results)
                    .build();
        } catch (Exception ex) {
            log.warn("File {} import failed", fileName, ex);
            String errorMessage = StringUtils.isEmpty(ex.getMessage())
                    ? "An unknown error occurred during file import"
                    : ex.getMessage();
            return ImportResourcesFileResult.builder()
                    .importResults(List.of())
                    .error(errorMessage)
                    .build();
        }
    }

    private ImportResourcesResult importZipFile(String rootPath,
                                                String filename,
                                                InputStream fileInputStream,
                                                ImportResources importFiles,
                                                SimpleCircuitBreaker circuitBreaker) {
        String sourcePath = null;
        String targetPath = null;
        try {
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
            boolean override = conflictResolutionStrategy == ImportConflictResolutionStrategy.OVERRIDE;
            var header = HeaderUtils.createHeadersForCreate(override, null);
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
                    var archivePath = ExportPathUtils.toExportedFileStoragePath(path, exportEntries.get(path));
                    zos.putNextEntry(new ZipEntry("files/" + archivePath));
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
        return ResourceEximExportHelper.resolveExportEntries(
                exportResource.getPaths(),
                folder -> ResourceEximExportHelper.collectPathsUnderFolder(folder, this::getAll, "file"));
    }

}