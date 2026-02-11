package com.epam.aidial.cfg.service.publication.resolver;

import com.epam.aidial.cfg.client.dto.PublicationDto;
import com.epam.aidial.cfg.client.dto.ResourceTypeDto;
import com.epam.aidial.cfg.client.mapper.ConversationClientMapper;
import com.epam.aidial.cfg.client.mapper.PublicationClientMapper;
import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.model.ConversationPublicationResource;
import com.epam.aidial.cfg.model.Publication;
import com.epam.aidial.cfg.model.PublicationMissingResource;
import com.epam.aidial.cfg.model.ResourceType;
import com.epam.aidial.cfg.service.ConversationService;
import com.epam.aidial.cfg.service.publication.resolver.url.PublicationResourceUrlResolver;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Component
@LogExecution
public class ConversationPublicationResolver extends PublicationResolver {

    private final PublicationClientMapper mapper;
    private final ConversationService conversationService;
    private final FilePublicationResolver filePublicationResolver;

    protected ConversationPublicationResolver(PublicationResourceUrlResolver resolver,
                                              PublicationClientMapper mapper,
                                              ConversationService conversationService, FilePublicationResolver filePublicationResolver) {
        super(resolver);
        this.mapper = mapper;
        this.conversationService = conversationService;
        this.filePublicationResolver = filePublicationResolver;
    }

    @Override
    public Publication resolvePublication(PublicationDto publicationDto) {
        checkForNotApplicableResourceTypes(publicationDto);

        var resourceInfoList = publicationDto.getResources().stream()
                .map(resourceInfo(publicationDto.getStatus()))
                .toList();
        List<PublicationMissingResource> missingResources = new ArrayList<>();

        var conversations = resourceInfoList.stream()
                .filter(resourceUrlStartsWith(ConversationClientMapper.CONVERSATIONS_PREFIX))
                .map(resource -> resolveResourceAndCollectMissing(
                        () -> getConversationPublication(resource),
                        ResourceType.CONVERSATION,
                        extractConversationPath(resource),
                        missingResources,
                        "Conversation not found"))
                .flatMap(Optional::stream)
                .toList();

        var files = filePublicationResolver.resolveFileResourcePaths(resourceInfoList, missingResources);

        return mapper.toConversationPublication(publicationDto, conversations, files, missingResources);
    }

    @Override
    public PublicationDto resolveUpdatePublication(Publication publication, List<MultipartFile> files) {
        throw new UnsupportedOperationException("Operation not supported");
    }

    @Override
    public ResourceType getResourceType() {
        return ResourceType.CONVERSATION;
    }

    @Override
    public Set<ResourceTypeDto> applicableResourceTypes() {
        return Set.of(ResourceTypeDto.CONVERSATION, ResourceTypeDto.FILE);
    }

    private ConversationPublicationResource getConversationPublication(ResourceInfo resourceInfo) {
        var resource = resourceInfo.resource();
        var conversationPath = extractConversationPath(resourceInfo);
        var conversation = conversationService.getConversation(conversationPath);
        return mapper.toConversationPublicationResource(resource, conversation);
    }

    private String extractConversationPath(ResourceInfo resourceInfo) {
        return extractPath(resourceInfo, ConversationClientMapper.CONVERSATIONS_PREFIX);
    }

}