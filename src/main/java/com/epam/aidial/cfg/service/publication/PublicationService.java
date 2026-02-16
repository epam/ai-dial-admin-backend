package com.epam.aidial.cfg.service.publication;

import com.epam.aidial.cfg.client.PublicationClient;
import com.epam.aidial.cfg.client.dto.CreatePublicationDto;
import com.epam.aidial.cfg.client.dto.PublicationDto;
import com.epam.aidial.cfg.client.dto.PublicationStatusDto;
import com.epam.aidial.cfg.client.dto.ResourceTypeDto;
import com.epam.aidial.cfg.client.dto.RuleRequest;
import com.epam.aidial.cfg.client.dto.RulesDto;
import com.epam.aidial.cfg.client.mapper.ApplicationClientMapper;
import com.epam.aidial.cfg.client.mapper.ConversationClientMapper;
import com.epam.aidial.cfg.client.mapper.FileClientMapper;
import com.epam.aidial.cfg.client.mapper.PromptClientMapper;
import com.epam.aidial.cfg.client.mapper.PublicationClientMapper;
import com.epam.aidial.cfg.client.mapper.ToolSetClientMapper;
import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.exception.EntityNotFoundException;
import com.epam.aidial.cfg.model.ApplicationPublicationResource;
import com.epam.aidial.cfg.model.ConversationPublicationResource;
import com.epam.aidial.cfg.model.CreatePublication;
import com.epam.aidial.cfg.model.FilePublicationResource;
import com.epam.aidial.cfg.model.PromptPublicationResource;
import com.epam.aidial.cfg.model.Publication;
import com.epam.aidial.cfg.model.PublicationInfos;
import com.epam.aidial.cfg.model.PublicationResource;
import com.epam.aidial.cfg.model.ResourceType;
import com.epam.aidial.cfg.model.Rule;
import com.epam.aidial.cfg.model.ToolSetPublicationResource;
import com.epam.aidial.cfg.service.publication.resolver.PublicationResolver;
import com.epam.aidial.cfg.service.publication.resolver.type.PublicationResourceTypeResolver;
import com.epam.aidial.cfg.utils.PathUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.jetbrains.annotations.Nullable;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.epam.aidial.cfg.client.mapper.CoreMetadataUtils.encodeFolderPath;

@Service
@LogExecution
@RequiredArgsConstructor
public class PublicationService {

    private static final Set<PublicationStatusDto> PUBLICATION_NOT_FOUND_STATUSES = Set.of(
            PublicationStatusDto.APPROVED,
            PublicationStatusDto.REJECTED
    );

    private final PublicationClient publicationClient;
    private final PublicationClientMapper mapper;
    private final PublicationResourceTypeResolver publicationResourceTypeResolver;
    private final Map<ResourceType, PublicationResolver> publicationResolversByResourceType;

    public PublicationInfos getAllPublications(@Nullable ResourceType resourceType) {
        var pathDto = mapper.toPublicationsPathDto("publications/public/");
        var publicationInfosDto = publicationClient.getPublications(pathDto);

        var publications = publicationInfosDto.getPublications().stream()
                .filter(publication -> hasCorrectResourceType(publication.getResourceTypes(), resourceType))
                .collect(Collectors.toList());

        return mapper.toPublicationInfos(publicationInfosDto, publications);
    }

    public Publication getPublication(String path) {
        var pathDto = mapper.toPublicationPathDto(path);
        var publicationDto = publicationClient.getPublication(pathDto);

        if (PUBLICATION_NOT_FOUND_STATUSES.contains(publicationDto.getStatus())) {
            throw new EntityNotFoundException("Publication not found: %s".formatted(path));
        }

        return resolvePublication(publicationDto);
    }

    public void updatePublication(Publication publication, List<MultipartFile> files) {
        updateTargetFolder(publication);
        var publicationDto = updatePublicationResources(publication, files);
        publicationClient.updatePublication(publicationDto);
    }

    public void approvePublication(String path) {
        var pathDto = mapper.toPublicationPathDto(path);
        publicationClient.approvePublication(pathDto);
    }

    public void rejectPublication(String path, String comment) {
        var sanitizedComment = Jsoup.clean(comment, Safelist.none());
        var rejectPublicationDto = mapper.toRejectPublicationDto(path, sanitizedComment);
        publicationClient.rejectPublication(rejectPublicationDto);
    }

    public String createPublication(CreatePublication createPublication) {
        CreatePublicationDto dto = mapper.toCreatePublicationDto(createPublication);
        PublicationDto publication = publicationClient.createPublication(dto);
        return publication.getUrl();
    }

    public void deletePublication(String path) {
        var pathDto = mapper.toPublicationPathDto(path);
        publicationClient.deletePublication(pathDto);
    }

    public Map<String, List<Rule>> getRules(String path) {
        RuleRequest ruleRequest = new RuleRequest(encodeFolderPath(path));
        RulesDto rules = publicationClient.getRules(ruleRequest);
        if (rules == null || MapUtils.isEmpty(rules.getRules())) {
            return Collections.emptyMap();
        }
        return mapper.toRules(rules.getRules());
    }

    private Publication resolvePublication(PublicationDto publicationDto) {
        var resourceTypes = CollectionUtils.emptyIfNull(publicationDto.getResourceTypes());
        var publicationResolver = getPublicationResolver(resourceTypes);
        return publicationResolver.resolvePublication(publicationDto);
    }

    private PublicationDto updatePublicationResources(Publication publication, List<MultipartFile> files) {
        var filesProvided = CollectionUtils.isNotEmpty(files);
        var resourceTypes = getResourcesTypes(publication, filesProvided);
        var publicationResolver = getPublicationResolver(resourceTypes);
        return publicationResolver.updatePublicationResources(publication, files);
    }

    private PublicationResolver getPublicationResolver(Collection<ResourceTypeDto> resourceTypes) {
        var resourceType = publicationResourceTypeResolver.resolveResourceType(resourceTypes);
        if (resourceType == null) {
            throw new IllegalStateException("Unable to resolve publication resource type. Resource types: " + resourceTypes);
        }

        var resolver = publicationResolversByResourceType.get(resourceType);
        if (resolver == null) {
            throw new IllegalStateException("Unable to find publication resolver. Resource type: " + resourceType);
        }

        return resolver;
    }

    private boolean hasCorrectResourceType(List<ResourceTypeDto> resourceTypes, ResourceType resourceType) {
        if (resourceType == null) {
            return true;
        }
        if (CollectionUtils.isEmpty(resourceTypes)) {
            return false;
        }
        return resourceType == publicationResourceTypeResolver.resolveResourceType(resourceTypes);
    }

    private Set<ResourceTypeDto> getResourcesTypes(Publication publication, boolean isFilesProvided) {
        if (publication == null) {
            throw new IllegalStateException("Publication must not be null");
        }

        if (CollectionUtils.isEmpty(publication.getResources())) {
            return isFilesProvided
                    ? Set.of(ResourceTypeDto.FILE)
                    : Set.of();
        }

        return publication.getResources().stream()
                .map(mapper::getResourceType)
                .collect(Collectors.toSet());
    }

    private void updateTargetFolder(Publication publication) {
        var targetFolder = publication.getFolderId();
        publication.getResources().forEach(s -> updateTargetFolder(s, targetFolder, getPrefix(s)));
    }

    private void updateTargetFolder(PublicationResource resource, String targetFolder, String prefix) {
        var path = resource.getTargetUrl();
        var pathWithoutPrefix = path.startsWith(prefix)
                ? path.substring(prefix.length())
                : path;
        var name = PathUtils.parsePath(pathWithoutPrefix).getName();

        var normalizedPrefix = PathUtils.ensureTrailingSlash(prefix);
        var normalizedFolder = PathUtils.ensureTrailingSlash(targetFolder);

        resource.setTargetUrl(normalizedPrefix + normalizedFolder + name);
    }

    private String getPrefix(PublicationResource publicationResource) {
        if (publicationResource instanceof PromptPublicationResource) {
            return PromptClientMapper.PROMPTS_PREFIX;
        } else if (publicationResource instanceof FilePublicationResource) {
            return FileClientMapper.FILES_PREFIX;
        } else if (publicationResource instanceof ApplicationPublicationResource) {
            return ApplicationClientMapper.APPLICATIONS_PREFIX;
        } else if (publicationResource instanceof ConversationPublicationResource) {
            return ConversationClientMapper.CONVERSATIONS_PREFIX;
        } else if (publicationResource instanceof ToolSetPublicationResource) {
            return ToolSetClientMapper.TOOLSETS_PREFIX;
        }
        throw new IllegalArgumentException("Unsupported publication type: %s. Publication: %s"
                .formatted(publicationResource.getClass(), publicationResource));
    }

}