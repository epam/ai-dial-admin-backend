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
import com.epam.aidial.cfg.exception.EntityNotFoundException;
import com.epam.aidial.cfg.model.CreatePublication;
import com.epam.aidial.cfg.model.Publication;
import com.epam.aidial.cfg.model.PublicationInfos;
import com.epam.aidial.cfg.model.ResourceType;
import com.epam.aidial.cfg.model.Rule;
import com.epam.aidial.cfg.service.publication.resolver.PublicationResolver;
import com.epam.aidial.cfg.service.publication.resolver.type.PublicationResourceTypeResolver;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.jetbrains.annotations.Nullable;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;
import org.springframework.stereotype.Service;

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

    public void approvePublication(String path) {
        var pathDto = mapper.toPublicationPathDto(path);
        publicationClient.approvePublication(pathDto);
    }

    public void rejectPublication(String path, String comment) {
        var safeComment = Jsoup.clean(comment, Whitelist.none());
        var sanitizedComment = mapper.toRejectPublicationDto(path, safeComment);
        publicationClient.rejectPublication(sanitizedComment);
    }

    public String createPublication(CreatePublication createPublication) {
        CreatePublicationDto dto = mapper.toCreatePublicationDto(createPublication);
        PublicationDto publication = publicationClient.createPublication(dto);
        return publication.getUrl();
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
        var resourceType = publicationResourceTypeResolver.resolveResourceType(resourceTypes);

        if (resourceType == null) {
            throw new IllegalStateException("Unable to resolve publication resource type. Resource types: " + resourceTypes);
        }

        var publicationResolver = publicationResolversByResourceType.get(resourceType);

        if (publicationResolver == null) {
            throw new IllegalStateException("Unable to find publication resolver. Resource type: " + resourceType);
        }

        return publicationResolver.resolvePublication(publicationDto);
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

}
