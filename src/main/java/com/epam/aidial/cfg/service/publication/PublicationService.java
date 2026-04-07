package com.epam.aidial.cfg.service.publication;

import com.epam.aidial.cfg.client.PublicationClient;
import com.epam.aidial.cfg.client.dto.CreatePublicationDto;
import com.epam.aidial.cfg.client.dto.PublicationDto;
import com.epam.aidial.cfg.client.dto.PublicationStatusDto;
import com.epam.aidial.cfg.client.dto.ResourceTypeDto;
import com.epam.aidial.cfg.client.dto.RuleRequest;
import com.epam.aidial.cfg.client.dto.RulesDto;
import com.epam.aidial.cfg.client.mapper.PublicationClientMapper;
import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.dao.audit.listener.AuditParentActivityHolder;
import com.epam.aidial.cfg.domain.model.activity.ActivityType;
import com.epam.aidial.cfg.domain.service.AuditActivityLogService;
import com.epam.aidial.cfg.exception.EntityNotFoundException;
import com.epam.aidial.cfg.model.ApplicationPublication;
import com.epam.aidial.cfg.model.ConversationPublication;
import com.epam.aidial.cfg.model.CreatePublication;
import com.epam.aidial.cfg.model.FilePublication;
import com.epam.aidial.cfg.model.PromptPublication;
import com.epam.aidial.cfg.model.Publication;
import com.epam.aidial.cfg.model.PublicationInfos;
import com.epam.aidial.cfg.model.ResourceType;
import com.epam.aidial.cfg.model.Rule;
import com.epam.aidial.cfg.model.ToolSetPublication;
import com.epam.aidial.cfg.service.publication.resolver.PublicationResolver;
import com.epam.aidial.cfg.service.publication.resolver.type.PublicationResourceTypeResolver;
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
    private final AuditActivityLogService auditActivityLogService;
    private final AuditParentActivityHolder auditParentActivityHolder;

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
        var fileNames = CollectionUtils.isNotEmpty(files)
                ? files.stream()
                .map(MultipartFile::getOriginalFilename)
                .collect(Collectors.joining(","))
                : null;
        var parentId = auditActivityLogService.logPublicationUpdate(
                publication.getPath(),
                fileNames);
        try (var scope = auditParentActivityHolder.openScope(parentId)) {
            var resourceType = getPublicationType(publication);
            var publicationResolver = getPublicationResolver(List.of(resourceType));
            publicationResolver.attachUploadedFiles(publication, files);
            var publicationDto = publicationResolver.updatePublicationResourceTargets(publication);
            publicationClient.updatePublication(publicationDto);
            publicationResolver.updatePublicationResources(publication);
            auditActivityLogService.logPublication(publication.getPath(), ActivityType.Update, null);
        }
    }

    public void approvePublication(String path) {
        var pathDto = mapper.toPublicationPathDto(path);
        publicationClient.approvePublication(pathDto);
        auditActivityLogService.logPublication(path, ActivityType.PublicationApprove, null);
    }

    public void rejectPublication(String path, String comment) {
        var sanitizedComment = Jsoup.clean(comment, Safelist.none());
        var rejectPublicationDto = mapper.toRejectPublicationDto(path, sanitizedComment);
        publicationClient.rejectPublication(rejectPublicationDto);
        auditActivityLogService.logPublication(path, ActivityType.PublicationReject, sanitizedComment);
    }

    public String createPublication(CreatePublication createPublication) {
        CreatePublicationDto dto = mapper.toCreatePublicationDto(createPublication);
        PublicationDto publication = publicationClient.createPublication(dto);
        auditActivityLogService.logPublicationCreate(publication, ActivityType.Create, null);
        return publication.getUrl();
    }

    public void deletePublication(String path) {
        var pathDto = mapper.toPublicationPathDto(path);
        publicationClient.deletePublication(pathDto);
        auditActivityLogService.logPublication(path, ActivityType.Delete, null);
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

    private ResourceTypeDto getPublicationType(Publication publication) {
        if (publication instanceof PromptPublication) {
            return ResourceTypeDto.PROMPT;
        } else if (publication instanceof FilePublication) {
            return ResourceTypeDto.FILE;
        } else if (publication instanceof ApplicationPublication) {
            return ResourceTypeDto.APPLICATION;
        } else if (publication instanceof ConversationPublication) {
            return ResourceTypeDto.CONVERSATION;
        } else if (publication instanceof ToolSetPublication) {
            return ResourceTypeDto.TOOL_SET;
        }
        throw new IllegalArgumentException("Unsupported publication type: %s. Publication: %s"
                .formatted(publication.getClass(), publication));
    }

}