package com.epam.aidial.cfg.service.publication.resolver;

import com.epam.aidial.cfg.client.dto.PublicationDto;
import com.epam.aidial.cfg.client.dto.PublicationResourceActionDto;
import com.epam.aidial.cfg.client.dto.PublicationResourceDto;
import com.epam.aidial.cfg.client.dto.PublicationStatusDto;
import com.epam.aidial.cfg.client.dto.ResourceTypeDto;
import com.epam.aidial.cfg.client.mapper.PromptClientMapper;
import com.epam.aidial.cfg.client.mapper.PublicationClientMapper;
import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.exception.ResourceAlreadyExistsException;
import com.epam.aidial.cfg.model.PromptPublication;
import com.epam.aidial.cfg.model.PromptPublicationResource;
import com.epam.aidial.cfg.model.Publication;
import com.epam.aidial.cfg.model.PublicationResourceIssue;
import com.epam.aidial.cfg.model.ResourceType;
import com.epam.aidial.cfg.service.prompt.PromptService;
import com.epam.aidial.cfg.service.publication.resolver.url.PublicationResourceUrlResolver;
import com.epam.aidial.cfg.utils.PathUtils;
import com.epam.aidial.core.util.UrlUtil;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Component
@LogExecution
public class PromptPublicationResolver extends PublicationResolver {

    private final PublicationClientMapper mapper;
    private final PromptService promptService;
    private final PromptClientMapper promptClientMapper;

    protected PromptPublicationResolver(PublicationResourceUrlResolver resolver,
                                        PublicationClientMapper mapper,
                                        PromptService promptService,
                                        PromptClientMapper promptClientMapper) {
        super(resolver);
        this.mapper = mapper;
        this.promptService = promptService;
        this.promptClientMapper = promptClientMapper;
    }

    @Override
    public Publication resolvePublication(PublicationDto publicationDto) {
        checkForNotApplicableResourceTypes(publicationDto);
        List<PublicationResourceIssue> resourceIssues = new ArrayList<>();
        var promptResources = publicationDto.getResources().stream()
                .map(resourceInfo(publicationDto.getStatus()))
                .map(prompt -> resolveResourceAndCollectIssues(
                        () -> getPromptPublication(prompt, prompt.status()),
                        resourceIssues,
                        new PublicationResourceIssue(ResourceType.PROMPT, extractPromptPath(prompt.resource(), prompt.status()),
                                "Prompt not found"),
                        new PublicationResourceIssue(ResourceType.PROMPT, prompt.resource().getTargetUrl(),
                                "Target prompt already exists")))
                .flatMap(Optional::stream)
                .toList();
        return mapper.toPromptPublication(publicationDto, promptResources, resourceIssues);
    }

    @Override
    public void updatePublicationResources(Publication publication) {
        var promptPublication = (PromptPublication) publication;

        var prompts = promptPublication.getResources();

        prompts.stream()
                .map(PromptPublicationResource::getPrompt)
                .map(promptClientMapper::toCreatePrompt)
                .forEach(prompt -> promptService.putPrompt(prompt, true, null));
    }

    @Override
    public ResourceType getResourceType() {
        return ResourceType.PROMPT;
    }

    @Override
    public Set<ResourceTypeDto> applicableResourceTypes() {
        return Set.of(ResourceTypeDto.PROMPT, ResourceTypeDto.FILE);
    }

    @Override
    public PublicationDto updatePublicationResourceTargets(Publication publication) {
        var promptPublication = (PromptPublication) publication;
        var updatedResources = promptPublication.getResources().stream()
                .map(fileResource -> recalculateTargetUrl(fileResource, publication.getFolderId()))
                .toList();
        return mapper.toPublicationDto(publication, updatedResources);
    }

    private PromptPublicationResource recalculateTargetUrl(PromptPublicationResource resource, String folderId) {
        var folder = PathUtils.ensureTrailingSlash(folderId);
        var promptResource = resource.getPrompt();
        var newTargetPath = UrlUtil.encodePath(PathUtils.buildPath(PromptClientMapper.PROMPTS_PREFIX + folder,
                promptResource.getName(), promptResource.getVersion()));
        resource.setTargetUrl(newTargetPath);
        return resource;
    }

    private PromptPublicationResource getPromptPublication(ResourceInfo resourceInfo, PublicationStatusDto status) {
        validateTargetNotPublished(resourceInfo, status);
        var promptPath = extractPromptPath(resourceInfo.resource(), status);
        var prompt = promptService.getPrompt(promptPath);
        return mapper.toPromptPublicationResource(resourceInfo.resource(), prompt);
    }

    private String extractPromptPath(PublicationResourceDto publicationResource, PublicationStatusDto status) {
        var promptUrl = resolver.resolveUrl(publicationResource, status);
        return PromptClientMapper.parseEncodedVersionedPath(promptUrl).getPath();
    }

    private void validateTargetNotPublished(ResourceInfo resourceInfo, PublicationStatusDto status) {
        var insideResource = resourceInfo.resource();
        if (status == PublicationStatusDto.PENDING && insideResource.getAction() != PublicationResourceActionDto.DELETE) {
            var targetUrl = extractTargetPath(resourceInfo, PromptClientMapper.PROMPTS_PREFIX);
            validateNotPublishedAtPath(targetUrl);
        }
    }

    private void validateNotPublishedAtPath(String path) {
        if (promptService.promptExists(path)) {
            throw new ResourceAlreadyExistsException("Target prompt already exist");
        }
    }
}