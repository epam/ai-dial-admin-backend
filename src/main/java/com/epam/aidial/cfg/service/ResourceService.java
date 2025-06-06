package com.epam.aidial.cfg.service;

import com.epam.aidial.cfg.client.dto.BaseMetadataDto;
import com.epam.aidial.cfg.dto.NodeTypeDto;
import com.epam.aidial.cfg.model.FolderInfo;
import com.epam.aidial.cfg.model.ResourceMetadataRequest;
import feign.FeignException;
import org.apache.commons.collections4.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface ResourceService {

    FolderInfo getFolders(ResourceMetadataRequest request);

    BaseMetadataDto getMetadata(ResourceMetadataRequest request);

    default Set<String> getResourceUrls(String path) {
        try {
            ResourceMetadataRequest request = ResourceMetadataRequest.builder()
                    .path(path)
                    .recursive(true)
                    .build();
            BaseMetadataDto applicationMetadata = getMetadata(request);
            return getResourceUrls(applicationMetadata.getItems());
        } catch (FeignException.FeignClientException.NotFound notFound) {
            return Collections.emptySet();
        }
    }

    private Set<String> getResourceUrls(List<? extends BaseMetadataDto> items) {
        if (CollectionUtils.isEmpty(items)) {
            return Collections.emptySet();
        }
        return items.stream()
                .flatMap(item -> {
                    if (Objects.equals(item.getNodeType(), NodeTypeDto.ITEM)) {
                        return Stream.of(item.getUrl());
                    } else if (Objects.equals(item.getNodeType(), NodeTypeDto.FOLDER)
                            && CollectionUtils.isNotEmpty(item.getItems())) {
                        return getResourceUrls(item.getItems()).stream();
                    }
                    return Stream.empty();
                })
                .collect(Collectors.toSet());
    }

}
