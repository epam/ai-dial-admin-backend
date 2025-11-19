package com.epam.aidial.cfg.service.publication.resolver;

import com.epam.aidial.cfg.client.dto.PublicationDto;
import com.epam.aidial.cfg.client.dto.ResourceTypeDto;
import com.epam.aidial.cfg.client.mapper.ConversationClientMapper;
import com.epam.aidial.cfg.client.mapper.FileClientMapper;
import com.epam.aidial.cfg.client.mapper.PublicationClientMapper;
import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.model.ConversationPublicationResource;
import com.epam.aidial.cfg.model.Publication;
import com.epam.aidial.cfg.model.ResourceType;
import com.epam.aidial.cfg.service.ConversationService;
import com.epam.aidial.cfg.service.publication.resolver.url.PublicationResourceUrlResolver;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@LogExecution
public class ConversationPublicationResolver extends PublicationResolver {

    private final PublicationClientMapper mapper;
    private final ConversationService conversationService;

    protected ConversationPublicationResolver(PublicationResourceUrlResolver resolver,
                                              PublicationClientMapper mapper,
                                              ConversationService conversationService) {
        super(resolver);
        this.mapper = mapper;
        this.conversationService = conversationService;
    }

    @Override
    public Publication resolvePublication(PublicationDto publicationDto) {
        checkForNotApplicableResourceTypes(publicationDto);

        var resourceInfoList = publicationDto.getResources().stream()
                .map(resourceInfo(publicationDto.getStatus()))
                .toList();

        var conversations = resourceInfoList.stream()
                .filter(resourceUrlStartsWith(ConversationClientMapper.CONVERSATIONS_PREFIX))
                .map(this::getConversationPublication)
                .toList();
        var files = resourceInfoList.stream()
                .filter(resourceUrlStartsWith(FileClientMapper.FILES_PREFIX))
                .map(this::extractFilePath)
                .toList();

        return mapper.toConversationPublication(publicationDto, conversations, files);
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
        return mapper.toConversationPublicationResource(resource.getAction(), conversation);
    }

    private String extractConversationPath(ResourceInfo resourceInfo) {
        return extractPath(resourceInfo, ConversationClientMapper.CONVERSATIONS_PREFIX);
    }

}
