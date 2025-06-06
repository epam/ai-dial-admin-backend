package com.epam.aidial.cfg.service;

import com.epam.aidial.cfg.client.ApplicationClient;
import com.epam.aidial.cfg.client.dto.ApplicationMetadataDto;
import com.epam.aidial.cfg.client.mapper.ApplicationClientMapper;
import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.model.ApplicationResource;
import com.epam.aidial.cfg.model.FolderInfo;
import com.epam.aidial.cfg.model.ResourceMetadataRequest;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static com.epam.aidial.cfg.client.mapper.ApplicationClientMapper.APPLICATIONS_PREFIX;

@Service
@RequiredArgsConstructor
@LogExecution
public class ApplicationService implements ResourceService {

    private final ApplicationClient applicationClient;
    private final ApplicationClientMapper mapper;

    public FolderInfo getFolders(ResourceMetadataRequest request) {
        try {
            ApplicationMetadataDto applicationMetadata = getMetadata(request);
            return mapper.toFolderInfo(applicationMetadata, APPLICATIONS_PREFIX);
        } catch (FeignException.FeignClientException.NotFound notFound) {
            return null;
        }
    }

    public ApplicationMetadataDto getMetadata(ResourceMetadataRequest request) {
        return applicationClient.getApplicationMetadata(request.getPath(), request.isRecursive(), request.getNextToken());
    }

    public ApplicationResource getApplicationResource(String path) {
        var applicationDto = applicationClient.getApplicationResource(path);
        var applicationMetadataDto = applicationClient.getApplicationMetadata(path, false, null);
        return mapper.toApplicationResource(applicationDto, applicationMetadataDto);
    }

}
