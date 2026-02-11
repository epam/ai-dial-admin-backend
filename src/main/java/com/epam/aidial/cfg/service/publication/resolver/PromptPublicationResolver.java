package com.epam.aidial.cfg.service.publication.resolver;

import com.epam.aidial.cfg.client.dto.PublicationDto;
import com.epam.aidial.cfg.client.dto.PublicationResourceDto;
import com.epam.aidial.cfg.client.dto.PublicationStatusDto;
import com.epam.aidial.cfg.client.dto.ResourceTypeDto;
import com.epam.aidial.cfg.client.mapper.PromptClientMapper;
import com.epam.aidial.cfg.client.mapper.PublicationClientMapper;
import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.model.PromptPublication;
import com.epam.aidial.cfg.model.PromptPublicationResource;
import com.epam.aidial.cfg.model.Publication;
import com.epam.aidial.cfg.model.PublicationMissingResource;
import com.epam.aidial.cfg.model.ResourceType;
import com.epam.aidial.cfg.service.prompt.PromptService;
import com.epam.aidial.cfg.service.publication.resolver.url.PublicationResourceUrlResolver;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Component
@LogExecution
public class PromptPublicationResolver extends PublicationResolver {

    private final PublicationClientMapper mapper;
    private final PromptService promptService;

    protected PromptPublicationResolver(PublicationResourceUrlResolver resolver,
                                        PublicationClientMapper mapper,
                                        PromptService promptService) {
        super(resolver);
        this.mapper = mapper;
        this.promptService = promptService;
    }

    @Override
    public Publication resolvePublication(PublicationDto publicationDto) {
        checkForNotApplicableResourceTypes(publicationDto);

        List<PublicationMissingResource> missingResources = new ArrayList<>();
        var promptResources = publicationDto.getResources().stream()
                .map(resourceInfo(publicationDto.getStatus()))
                .map(prompt -> resolveResourceAndCollectMissing(
                        () -> getPromptPublication(prompt.resource(), prompt.status()),
                        ResourceType.PROMPT,
                        extractPromptPath(prompt.resource(), prompt.status()),
                        missingResources,
                        "Prompt not found"))
                .flatMap(Optional::stream)
                .toList();
        return mapper.toPromptPublication(publicationDto, promptResources, missingResources);
    }

    @Override
    public PublicationDto resolveUpdatePublication(Publication publication, List<MultipartFile> files) {
        var promptPublication = (PromptPublication) publication;

        var prompts = promptPublication.getResources().stream()
                .filter(publicationResourceUrlStartsWith(PromptClientMapper.PROMPTS_PREFIX)).toList();

        prompts.stream()
                .map(PromptPublicationResource::getPrompt)
                .map(mapper::toCreatePrompt)
                .forEach(prompt -> promptService.putPrompt(prompt, true, null));

        var publicationResources = prompts.stream()
                .map(mapper::toPublicationResource)
                .toList();

        return mapper.toPublicationDto(publication, publicationResources);
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
        return mapper.toPromptPublicationResource(resource, prompt);
    }

    private String extractPromptPath(PublicationResourceDto publicationResource, PublicationStatusDto status) {
        var promptUrl = resolver.resolveUrl(publicationResource, status);
        return PromptClientMapper.parseEncodedVersionedPath(promptUrl).getPath();
    }
}