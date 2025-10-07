package com.epam.aidial.cfg.client.mapper;

import com.epam.aidial.cfg.client.dto.FileMetadataDto;
import com.epam.aidial.cfg.dto.NodeTypeDto;
import com.epam.aidial.cfg.model.FileNodeInfo;
import com.epam.aidial.cfg.model.NodeType;
import lombok.Builder;
import lombok.Data;
import org.mapstruct.Mapper;

import java.util.List;

import static com.epam.aidial.cfg.client.mapper.CoreMetadataUtils.extractFolderId;
import static com.epam.aidial.cfg.client.mapper.CoreMetadataUtils.extractPath;

@Mapper(componentModel = "spring")
public interface FileClientMapper {

    String FILES_PREFIX = "files/";

    default FileNodeInfo toFileInfo(FileMetadataDto dto) {
        if (dto == null) {
            return null;
        }

        return switch (dto.getNodeType()) {
            case FOLDER -> FileNodeInfo.builder()
                    .path(extractPath(dto.getUrl(), FILES_PREFIX))
                    .name(null)
                    .folderId(null)
                    .updateTime(dto.getUpdatedAt())
                    .author(dto.getAuthor())
                    .nodeType(toFileNodeType(dto.getNodeType()))
                    .nextToken(dto.getNextToken())
                    .items(toFilesInfo(dto.getItems()))
                    .build();
            case ITEM -> FileNodeInfo.builder()
                    .path(extractPath(dto.getUrl(), FILES_PREFIX))
                    .name(dto.getName())
                    .folderId(extractFolderId(dto.getUrl(), FILES_PREFIX))
                    .updateTime(dto.getUpdatedAt())
                    .author(dto.getAuthor())
                    .nodeType(toFileNodeType(dto.getNodeType()))
                    .nextToken(dto.getNextToken())
                    .contentType(dto.getContentType())
                    .contentLength(dto.getContentLength())
                    .build();
        };
    }

    NodeType toFileNodeType(NodeTypeDto dto);

    private List<FileNodeInfo> toFilesInfo(List<FileMetadataDto> dtoList) {
        if (dtoList == null) {
            return null;
        }
        return dtoList.stream().map(this::toFileInfo).toList();
    }

    default FilePathParts parsePath(String path) {
        var pathDecoded = extractPath(path, FILES_PREFIX);

        int lastSlashIndex = pathDecoded.lastIndexOf('/');
        if (lastSlashIndex == -1) {
            throw new IllegalArgumentException("The path does not contain a '/': %s".formatted(path));
        }

        var folderId = pathDecoded.substring(0, lastSlashIndex + 1);
        var name = pathDecoded.substring(lastSlashIndex + 1);

        return FilePathParts.builder()
                .path(pathDecoded)
                .folderId(folderId)
                .name(name)
                .build();
    }

    @Data
    @Builder
    class FilePathParts {
        private String path;
        private String folderId;
        private String name;
    }

}
