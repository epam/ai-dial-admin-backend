package com.epam.aidial.cfg.mapper;

import com.epam.aidial.cfg.dto.ApplicationPublicationDto;
import com.epam.aidial.cfg.dto.ApplicationResourceDto;
import com.epam.aidial.cfg.dto.ConversationDto;
import com.epam.aidial.cfg.dto.ConversationPublicationDto;
import com.epam.aidial.cfg.dto.FileInfoDto;
import com.epam.aidial.cfg.dto.FilePublicationDto;
import com.epam.aidial.cfg.dto.FilePublicationResourceDto;
import com.epam.aidial.cfg.dto.PromptDto;
import com.epam.aidial.cfg.dto.PromptPublicationDto;
import com.epam.aidial.cfg.dto.PromptPublicationResourceDto;
import com.epam.aidial.cfg.dto.PublicationDto;
import com.epam.aidial.cfg.dto.PublicationInfosDto;
import com.epam.aidial.cfg.dto.ResourceTypeDto;
import com.epam.aidial.cfg.dto.ToolSetResourceDto;
import com.epam.aidial.cfg.dto.ToolSetResourcePublicationDto;
import com.epam.aidial.cfg.model.ApplicationPublication;
import com.epam.aidial.cfg.model.ApplicationPublicationResource;
import com.epam.aidial.cfg.model.ApplicationResource;
import com.epam.aidial.cfg.model.Conversation;
import com.epam.aidial.cfg.model.ConversationPublication;
import com.epam.aidial.cfg.model.ConversationPublicationResource;
import com.epam.aidial.cfg.model.FileNodeInfo;
import com.epam.aidial.cfg.model.FilePublication;
import com.epam.aidial.cfg.model.FilePublicationResource;
import com.epam.aidial.cfg.model.Prompt;
import com.epam.aidial.cfg.model.PromptPublication;
import com.epam.aidial.cfg.model.PromptPublicationResource;
import com.epam.aidial.cfg.model.Publication;
import com.epam.aidial.cfg.model.PublicationInfos;
import com.epam.aidial.cfg.model.PublicationResource;
import com.epam.aidial.cfg.model.PublicationResourceAction;
import com.epam.aidial.cfg.model.ResourceType;
import com.epam.aidial.cfg.model.ToolSetPublication;
import com.epam.aidial.cfg.model.ToolSetPublicationResource;
import com.epam.aidial.cfg.model.ToolSetResource;
import com.epam.aidial.metric.util.CollectorsUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.Objects;

@Mapper(componentModel = "spring")
public interface PublicationMapper {

    PublicationInfosDto toPromptPublicationInfosDto(PublicationInfos model);

    default PublicationDto toPublicationDto(Publication model) {
        var action = getAction(model);

        if (model instanceof PromptPublication promptPublication) {
            return toPromptPublicationDto(promptPublication, action);
        } else if (model instanceof FilePublication filePublication) {
            return toFilePublicationDto(filePublication, action);
        } else if (model instanceof ApplicationPublication applicationPublication) {
            return toApplicationPublicationDto(applicationPublication, action);
        } else if (model instanceof ConversationPublication conversationPublication) {
            return toConversationPublicationDto(conversationPublication, action);
        } else if (model instanceof ToolSetPublication toolSetPublication) {
            return toToolSetPublicationDto(toolSetPublication, action);
        }
        throw new IllegalArgumentException("Unsupported publication type: %s. Publication: %s"
                .formatted(model.getClass(), model));
    }

    default PromptPublicationDto toPromptPublicationDto(PromptPublication model, PublicationResourceAction action) {
        var prompts = model.getResources()
                .stream()
                .filter(Objects::nonNull)
                .toList();

        return toPromptPublicationDto(model, action, prompts);
    }

    PromptPublicationDto toPromptPublicationDto(PromptPublication model, PublicationResourceAction action,
                                                List<PromptPublicationResource> prompts);

    PromptPublicationResourceDto toPromptPublicationResourceDto(PromptPublicationResource resource);

    default FilePublicationDto toFilePublicationDto(FilePublication model, PublicationResourceAction action) {
        var files = model.getResources()
                .stream()
                .filter(Objects::nonNull)
                .toList();

        return toFilePublicationDto(model, action, files);
    }

    FilePublicationDto toFilePublicationDto(FilePublication model, PublicationResourceAction action, List<FilePublicationResource> files);

    default ApplicationPublicationDto toApplicationPublicationDto(ApplicationPublication model, PublicationResourceAction action) {
        var applicationResources = model.getResources()
                .stream()
                .filter(Objects::nonNull)
                .toList();

        return toApplicationPublicationDto(model, action, applicationResources);
    }

    ApplicationPublicationDto toApplicationPublicationDto(ApplicationPublication model,
                                                          PublicationResourceAction action,
                                                          List<ApplicationPublicationResource> applicationResources);

    default ConversationPublicationDto toConversationPublicationDto(ConversationPublication model, PublicationResourceAction action) {
        var conversations = model.getResources()
                .stream()
                .filter(Objects::nonNull)
                .toList();

        return toConversationPublicationDto(model, action, conversations);
    }

    ConversationPublicationDto toConversationPublicationDto(ConversationPublication model,
                                                            PublicationResourceAction action,
                                                            List<ConversationPublicationResource> conversations);

    default ToolSetResourcePublicationDto toToolSetPublicationDto(ToolSetPublication model, PublicationResourceAction action) {
        var toolSetResources = model.getResources()
                .stream()
                .filter(Objects::nonNull)
                .toList();

        return toToolSetPublicationDto(model, action, toolSetResources);
    }

    ToolSetResourcePublicationDto toToolSetPublicationDto(ToolSetPublication model,
                                                          PublicationResourceAction action,
                                                          List<ToolSetPublicationResource> toolSetResources);

    private PublicationResourceAction getAction(Publication model) {
        var actions = model.getResources().stream()
                .filter(Objects::nonNull)
                .map(PublicationResource::getAction)
                .toList();

        if (actions.isEmpty()) {
            return null;
        }

        if (model instanceof FilePublication
                && actions.stream()
                .allMatch(a -> a == PublicationResourceAction.ADD || a == PublicationResourceAction.ADD_IF_ABSENT)) {
            return PublicationResourceAction.ADD;
        }

        return actions.stream()
                .distinct()
                .collect(CollectorsUtils.toSingleton(()
                        -> new IllegalStateException("Different actions found inside publication request. Publication: %s".formatted(model))))
                .orElseThrow(() -> new IllegalStateException("No actions found inside publication request. Publication: %s".formatted(model)));
    }

    PromptDto toPromptDto(Prompt model);

    FileInfoDto toFileInfoDto(FileNodeInfo model);

    ApplicationResourceDto toApplicationResourceDto(ApplicationResource model);

    ConversationDto toConversationDto(Conversation model);

    ToolSetResourceDto toToolSetResourceDto(ToolSetResource model);

    ResourceType toResourceType(ResourceTypeDto dto);

    default Publication toPublication(PublicationDto publicationDto) {

        if (publicationDto instanceof PromptPublicationDto promptPublicationDto) {
            return toPromptPublication(promptPublicationDto);
        } else if (publicationDto instanceof FilePublicationDto filePublicationDto) {
            return toFilePublication(filePublicationDto);
        } else if (publicationDto instanceof ApplicationPublicationDto applicationPublicationDto) {
            return toApplicationResourcePublication(applicationPublicationDto);
        } else if (publicationDto instanceof ConversationPublicationDto conversationPublicationDto) {
            return toConversationPublication(conversationPublicationDto);
        } else if (publicationDto instanceof ToolSetResourcePublicationDto toolSetPublicationDto) {
            return toToolSetPublication(toolSetPublicationDto);
        }
        throw new IllegalArgumentException("Unsupported publication type: %s. Publication: %s"
                .formatted(publicationDto.getClass(), publicationDto));
    }

    @Mapping(target = "resources", source = "toolSetResources")
    ToolSetPublication toToolSetPublication(ToolSetResourcePublicationDto toolSetPublicationDto);

    @Mapping(target = "url", ignore = true)
    ToolSetResource toToolSetResource(ToolSetResourceDto toolSetResourceDto);

    @Mapping(target = "resources", source = "conversations")
    ConversationPublication toConversationPublication(ConversationPublicationDto conversationPublicationDto);

    @Mapping(target = "resources", source = "applicationResources")
    ApplicationPublication toApplicationResourcePublication(ApplicationPublicationDto applicationPublicationDto);

    @Mapping(target = "validityState", ignore = true)
    ApplicationResource toApplicationResource(ApplicationResourceDto applicationResourceDto);

    @Mapping(target = "resources", source = "prompts")
    PromptPublication toPromptPublication(PromptPublicationDto promptPublicationDto);

    @Mapping(target = "resources", source = "files")
    FilePublication toFilePublication(FilePublicationDto dto);

    @Mapping(target = "action", source = "action")
    @Mapping(target = "file", source = "file")
    @Mapping(target = "file.nodeType", constant = "ITEM")
    @Mapping(target = "file.nextToken", ignore = true)
    @Mapping(target = "file.items", ignore = true)
    FilePublicationResource toFilePublicationResource(FilePublicationResourceDto filePublicationResourceDto);
}