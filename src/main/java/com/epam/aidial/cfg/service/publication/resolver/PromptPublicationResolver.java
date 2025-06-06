package com.epam.aidial.cfg.service.publication.resolver;

import com.epam.aidial.cfg.client.dto.PublicationDto;
import com.epam.aidial.cfg.client.dto.PublicationResourceDto;
import com.epam.aidial.cfg.client.dto.PublicationStatusDto;
import com.epam.aidial.cfg.client.dto.ResourceTypeDto;
import com.epam.aidial.cfg.client.mapper.PromptClientMapper;
import com.epam.aidial.cfg.client.mapper.PublicationClientMapper;
import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.model.PromptPublicationResource;
import com.epam.aidial.cfg.model.Publication;
import com.epam.aidial.cfg.model.ResourceType;
import com.epam.aidial.cfg.service.prompt.PromptService;
import com.epam.aidial.cfg.service.publication.resolver.url.PublicationResourceUrlResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
@LogExecution
public class PromptPublicationResolver implements PublicationResolver {

    private final PublicationClientMapper mapper;
    private final PromptService promptService;
    private final PublicationResourceUrlResolver publicationResourceUrlResolver;

    @Override
    public Publication resolvePublication(PublicationDto publicationDto) {
        checkForNotApplicableResourceTypes(publicationDto);

        var promptResources = publicationDto.getResources().stream()
                .map(resource -> getPromptPublication(resource, publicationDto.getStatus()))
                .toList();
        return mapper.toPromptPublication(publicationDto, promptResources);
    }

    @Override
    public ResourceType getResourceType() {
        return ResourceType.PROMPT;
    }

    @Override
    public Set<ResourceTypeDto> applicableResourceTypes() {
        return Set.of(ResourceTypeDto.PROMPT, ResourceTypeDto.FILE);
    }

    private PromptPublicationResource getPromptPublication(PublicationResourceDto resource, PublicationStatusDto status) {
        var promptPath = extractPromptPath(resource, status);
        var prompt = promptService.getPrompt(promptPath);
        return mapper.toPromptPublicationResource(resource.getAction(), prompt);
    }

    private String extractPromptPath(PublicationResourceDto publicationResource, PublicationStatusDto status) {
        var promptUrl = publicationResourceUrlResolver.resolveUrl(publicationResource, status);
        return PromptClientMapper.parseEncodedVersionedPath(promptUrl).getPath();
    }
}
