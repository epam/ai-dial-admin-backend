package com.epam.aidial.cfg.web.facade;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.dto.ExportDto;
import com.epam.aidial.cfg.dto.FileNodeInfoDto;
import com.epam.aidial.cfg.dto.FilePathDto;
import com.epam.aidial.cfg.dto.FilePathsDto;
import com.epam.aidial.cfg.dto.ImportResourcesDto;
import com.epam.aidial.cfg.dto.ImportResourcesFileResultDto;
import com.epam.aidial.cfg.dto.MoveResourceDto;
import com.epam.aidial.cfg.dto.ResourceMetadataRequestDto;
import com.epam.aidial.cfg.mapper.FileMapper;
import com.epam.aidial.cfg.mapper.ResourceMapper;
import com.epam.aidial.cfg.model.ImportResources;
import com.epam.aidial.cfg.model.ResourceMetadataRequest;
import com.epam.aidial.cfg.model.UpdateRulesRequest;
import com.epam.aidial.cfg.service.FileService;
import com.epam.aidial.cfg.service.FolderService;
import feign.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.util.List;

@Service
@RequiredArgsConstructor
@LogExecution
@Slf4j
public class FileResourceFacade {
    private final FolderService folderService;
    private final FileService fileService;
    private final FileMapper fileMapper;
    private final ResourceMapper resourceMapper;

    public FileNodeInfoDto getAll(ResourceMetadataRequestDto requestDto) {
        ResourceMetadataRequest request = resourceMapper.toRequest(requestDto);
        var filesInfo = fileService.getAll(request);
        return fileMapper.toFilesInfoDto(filesInfo);
    }

    public Response getByPath(String path) {
        return fileService.get(path);
    }

    public ImportResourcesFileResultDto uploadFiles(List<MultipartFile> files,
                                                    ImportResourcesDto importFilesDto) {
        var importFiles = resourceMapper.toImportResources(importFilesDto);
        importFolderRules(importFiles);
        var importResults = fileService.uploadFile(files, importFiles);
        return resourceMapper.toImportResourcesFileResultDto(importResults);
    }

    public ImportResourcesFileResultDto uploadFilesZip(MultipartFile files,
                                                       ImportResourcesDto importFilesDto) {
        var importFiles = resourceMapper.toImportResources(importFilesDto);
        importFolderRules(importFiles);
        var importResult = fileService.uploadFileZip(importFiles, files);
        return resourceMapper.toImportResourcesFileResultDto(importResult);
    }

    public void deleteFile(String path) {
        fileService.deleteFile(path);
    }

    public void deleteFiles(FilePathsDto filePaths) {
        var paths = filePaths.getPaths().stream().map(FilePathDto::getPath).toList();
        fileService.deleteFiles(paths);
    }

    public void moveFile(MoveResourceDto moveResourceDto) {
        var moveResource = resourceMapper.toMoveResource(moveResourceDto);
        fileService.move(moveResource);
    }

    public StreamingResponseBody export(ExportDto exportDto) {
        return fileService.export(exportDto.getPaths());
    }

    private void importFolderRules(ImportResources importFiles) {
        if (importFiles.getRules() != null) {
            var updateRulesRequest = UpdateRulesRequest.builder()
                    .targetFolder(importFiles.getPath())
                    .rules(importFiles.getRules())
                    .build();
            folderService.updatesRules(updateRulesRequest);
        }
    }

}
