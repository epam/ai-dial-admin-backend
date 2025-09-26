package com.epam.aidial.cfg.service;

import com.epam.aidial.cfg.client.FileClient;
import com.epam.aidial.cfg.client.ResourceClient;
import com.epam.aidial.cfg.client.dto.FileMetadataDto;
import com.epam.aidial.cfg.client.mapper.FileClientMapper;
import com.epam.aidial.cfg.client.mapper.FolderMapper;
import com.epam.aidial.cfg.client.mapper.ResourceClientMapper;
import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.model.FileNodeInfo;
import com.epam.aidial.cfg.model.FolderInfo;
import com.epam.aidial.cfg.model.ImportConflictResolutionStrategy;
import com.epam.aidial.cfg.model.ImportResources;
import com.epam.aidial.cfg.model.ImportResourcesFileResult;
import com.epam.aidial.cfg.model.ImportResourcesResult;
import com.epam.aidial.cfg.model.MoveResource;
import com.epam.aidial.cfg.model.ResourceMetadataRequest;
import com.epam.aidial.cfg.model.ResourceType;
import com.epam.aidial.cfg.security.AuthorizationTokenHolder;
import com.epam.aidial.cfg.security.AuthorizationTokenWrapper;
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

    private final FileClient fileClient;
    private final FileClientMapper fileClientMapper;
    private final ResourceClient resourceClient;
    private final ResourceClientMapper resourceClientMapper;
    private final FolderMapper folderMapper;

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
        } catch (FeignException.FeignClientException.NotFound notFound) {
            return null;
        }
    }

    @Override
    public FileMetadataDto getMetadata(ResourceMetadataRequest request) {
        return fileClient.getFilesMetadata(request.getPath(), request.isRecursive(), request.getNextToken());
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

    private Map<String, String> getUploadHeader(ImportConflictResolutionStrategy strategy) {
        return switch (strategy) {
            case SKIP -> Map.of("If-None-Match", "*");
            case OVERRIDE -> Map.of("If-Match", "*");
        };
    }

    public ImportResourcesFileResult uploadFileZip(ImportResources importFiles, MultipartFile zipFile) {
        try {
            var rootPath = importFiles.getPath();
            var rootPathStripped = StringUtils.stripEnd(rootPath, "/");
            var inputStream = zipFile.getInputStream();
            var conflictResolutionStrategy = importFiles.getConflictResolutionStrategy();
            var circuitBreaker = new SimpleCircuitBreaker(importErrorsThreshold);

            var results = new ArrayList<ImportResourcesResult>();
            try (ZipInputStream zipInputStream = new ZipInputStream(inputStream)) {
                ZipEntry zipEntry;
                while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                    var result = importZipFile(rootPathStripped, zipEntry, zipInputStream, conflictResolutionStrategy,
                            circuitBreaker);
                    results.add(result);
                }
            }
            return ImportResourcesFileResult.builder()
                    .importResults(results)
                    .build();
        } catch (Exception ex) {
            String fileName = zipFile == null ? "not specified" : zipFile.getOriginalFilename();
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
                                                ZipEntry zipEntry,
                                                InputStream fileInputStream,
                                                ImportConflictResolutionStrategy conflictResolutionStrategy,
                                                SimpleCircuitBreaker circuitBreaker) {
        String sourcePath = null;
        String targetPath = null;
        try {
            var filename = zipEntry.getName();
            sourcePath = StringUtils.removeStart(filename, "files/");
            var sourcePathWithoutPublic = StringUtils.removeStart(sourcePath, "public/");
            targetPath = rootPath + "/" + sourcePathWithoutPublic;
            byte[] fileData = fileInputStream.readAllBytes();

            String contentTypeFromName = URLConnection.guessContentTypeFromName(filename);
            MultipartFile extractedFile = new MockMultipartFile("file", filename, contentTypeFromName, fileData);
            var result = createFileWithCircuitBreaker(extractedFile, sourcePath, targetPath, conflictResolutionStrategy, circuitBreaker);
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
        fileClient.deleteFile(path);
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

    public StreamingResponseBody export(List<String> paths) {
        var distinctPaths = paths.stream()
                .distinct()
                .sorted()
                .toList();

        var token = AuthorizationTokenHolder.getToken();
        return outputStream -> {
            try (
                    var ignored = new AuthorizationTokenWrapper(token);
                    var zos = new ZipOutputStream(outputStream)
            ) {

                for (var path : distinctPaths) {
                    var fileResponse = get(path);

                    zos.putNextEntry(new ZipEntry("files/" + path));
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
}
