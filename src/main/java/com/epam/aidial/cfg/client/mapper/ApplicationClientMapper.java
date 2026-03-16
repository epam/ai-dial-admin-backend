package com.epam.aidial.cfg.client.mapper;

import com.epam.aidial.cfg.client.dto.ApplicationMetadataDto;
import com.epam.aidial.cfg.client.dto.ApplicationResourceDto;
import com.epam.aidial.cfg.client.dto.NodeTypeDto;
import com.epam.aidial.cfg.dto.ApplicationEximDto;
import com.epam.aidial.cfg.model.ApplicationExim;
import com.epam.aidial.cfg.model.ApplicationResource;
import com.epam.aidial.cfg.model.ApplicationResourceNodeInfo;
import com.epam.aidial.cfg.model.CreateApplicationResource;
import com.epam.aidial.cfg.model.NodeType;
import com.epam.aidial.cfg.model.ValidityStateResource;
import com.epam.aidial.cfg.utils.PathUtils;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

import static com.epam.aidial.cfg.client.mapper.CoreMetadataUtils.extractPath;
import static com.epam.aidial.cfg.client.mapper.CoreMetadataUtils.parseEncodedVersionedPath;

@Mapper(componentModel = "spring", uses = {RouteMapper.class})
@Slf4j
public abstract class ApplicationClientMapper {
    public static final String APPLICATIONS_PREFIX = "applications/";

    public ApplicationResource toApplicationResource(ApplicationResourceDto applicationResourceDto,
                                                     ApplicationMetadataDto metadataDto,
                                                     ValidityStateResource validityState) {
        if (applicationResourceDto == null || metadataDto == null) {
            return null;
        }
        if (metadataDto.getNodeType() != NodeTypeDto.ITEM) {
            log.error("Metadata: {} must have item node type", metadataDto);
            throw new IllegalStateException("Metadata must have item node type");
        }

        var itemParts = PathUtils.parseEncodedVersionedPath(metadataDto.getUrl(), APPLICATIONS_PREFIX);
        return toApplicationResource(applicationResourceDto, metadataDto, itemParts, validityState);
    }

    @Mapping(target = "name", source = "itemParts.name")
    @Mapping(target = "updatedAt", source = "metadataDto.updatedAt")
    @Mapping(target = "folderId", source = "itemParts.folderId")
    @Mapping(target = "author", source = "metadataDto.author")
    @Mapping(target = "routes", source = "dto.routes")
    @Mapping(target = "validityState", source = "validityState")
    protected abstract ApplicationResource toApplicationResource(ApplicationResourceDto dto,
                                                                 ApplicationMetadataDto metadataDto,
                                                                 PathUtils.VersionedPathParts itemParts,
                                                                 ValidityStateResource validityState);

    public ApplicationResourceNodeInfo toApplicationInfo(ApplicationMetadataDto dto) {
        if (dto == null) {
            return null;
        }

        return switch (dto.getNodeType()) {
            case FOLDER -> ApplicationResourceNodeInfo.builder()
                    .path(extractPath(dto.getUrl(), APPLICATIONS_PREFIX))
                    .name(null)
                    .version(null)
                    .folderId(null)
                    .updatedAt(dto.getUpdatedAt())
                    .author(dto.getAuthor())
                    .nodeType(toNodeType(dto.getNodeType()))
                    .nextToken(dto.getNextToken())
                    .items(toApplicationInfo(dto.getItems()))
                    .build();
            case ITEM -> {
                var itemParts = parseEncodedVersionedPath(dto.getUrl(), APPLICATIONS_PREFIX);
                yield ApplicationResourceNodeInfo.builder()
                        .path(itemParts.getPath())
                        .name(itemParts.getName())
                        .version(itemParts.getVersion())
                        .folderId(itemParts.getFolderId())
                        .updatedAt(dto.getUpdatedAt())
                        .author(dto.getAuthor())
                        .nodeType(toNodeType(dto.getNodeType()))
                        .nextToken(dto.getNextToken())
                        .items(toApplicationInfo(dto.getItems()))
                        .build();
            }
        };
    }

    private List<ApplicationResourceNodeInfo> toApplicationInfo(List<ApplicationMetadataDto> dtoList) {
        if (dtoList == null) {
            return null;
        }
        return dtoList.stream().map(this::toApplicationInfo).toList();
    }

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "author", ignore = true)
    @Mapping(target = "invalid", ignore = true)
    public abstract ApplicationResourceDto toApplicationResourceDto(CreateApplicationResource createApplicationResource);

    protected abstract NodeType toNodeType(NodeTypeDto dto);

    public abstract ApplicationExim toApplicationExim(ApplicationResource applicationResource);

    @Mapping(target = "name", source = "itemParts.name")
    @Mapping(target = "folderId", source = "itemParts.folderId")
    @Mapping(target = "version", source = "itemParts.version")
    @Mapping(target = "routes", source = "dto.routes")
    public abstract CreateApplicationResource toCreateApplicationResource(ApplicationEximDto dto, PathUtils.VersionedPathParts itemParts);

    public abstract CreateApplicationResource toCreateApplicationResource(ApplicationResource applicationResource);

}