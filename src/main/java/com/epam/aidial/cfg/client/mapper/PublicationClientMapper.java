package com.epam.aidial.cfg.client.mapper;

import com.epam.aidial.cfg.client.dto.CreatePublicationDto;
import com.epam.aidial.cfg.client.dto.PublicationDto;
import com.epam.aidial.cfg.client.dto.PublicationInfoDto;
import com.epam.aidial.cfg.client.dto.PublicationInfosDto;
import com.epam.aidial.cfg.client.dto.PublicationPathDto;
import com.epam.aidial.cfg.client.dto.PublicationResourceActionDto;
import com.epam.aidial.cfg.client.dto.PublicationResourceDto;
import com.epam.aidial.cfg.client.dto.PublicationsPathDto;
import com.epam.aidial.cfg.client.dto.RejectPublicationsDto;
import com.epam.aidial.cfg.client.dto.RuleDto;
import com.epam.aidial.cfg.model.ApplicationPublication;
import com.epam.aidial.cfg.model.ApplicationPublicationResource;
import com.epam.aidial.cfg.model.ApplicationResource;
import com.epam.aidial.cfg.model.Conversation;
import com.epam.aidial.cfg.model.ConversationPublication;
import com.epam.aidial.cfg.model.ConversationPublicationResource;
import com.epam.aidial.cfg.model.CreatePublication;
import com.epam.aidial.cfg.model.FileNodeInfo;
import com.epam.aidial.cfg.model.FilePublication;
import com.epam.aidial.cfg.model.FilePublicationResource;
import com.epam.aidial.cfg.model.Prompt;
import com.epam.aidial.cfg.model.PromptPublication;
import com.epam.aidial.cfg.model.PromptPublicationResource;
import com.epam.aidial.cfg.model.PublicationInfo;
import com.epam.aidial.cfg.model.PublicationInfos;
import com.epam.aidial.cfg.model.PublicationResource;
import com.epam.aidial.cfg.model.Rule;
import com.epam.aidial.cfg.model.ToolSetPublication;
import com.epam.aidial.cfg.model.ToolSetPublicationResource;
import com.epam.aidial.cfg.model.ToolSetResource;
import org.apache.commons.collections4.MapUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface PublicationClientMapper {

    String PUBLICATIONS_PREFIX = "publications/";

    PublicationsPathDto toPublicationsPathDto(String url);

    @Mapping(target = "publications", source = "publications")
    PublicationInfos toPublicationInfos(PublicationInfosDto dto, List<PublicationInfoDto> publications);

    default PublicationInfo toPromptPublicationInfo(PublicationInfoDto dto) {
        var path = removePrefix(dto.getUrl(), PUBLICATIONS_PREFIX);
        return toPromptPublicationInfo(dto, path);
    }

    @Mapping(target = "requestName", source = "dto.name")
    PublicationInfo toPromptPublicationInfo(PublicationInfoDto dto, String path);

    default PublicationPathDto toPublicationPathDto(String path) {
        return PublicationPathDto.builder()
                .url(PUBLICATIONS_PREFIX + path)
                .build();
    }

    default RejectPublicationsDto toRejectPublicationDto(String path, String comment) {
        return RejectPublicationsDto.builder()
                .url(PUBLICATIONS_PREFIX + path)
                .comment(comment)
                .build();
    }

    default PromptPublication toPromptPublication(PublicationDto dto, List<PromptPublicationResource> resources) {
        var path = removePrefix(dto.getUrl(), PUBLICATIONS_PREFIX);
        return toPromptPublication(dto, path, resources);
    }

    @Mapping(target = "requestName", source = "dto.name")
    @Mapping(target = "folderId", source = "dto.targetFolder")
    @Mapping(target = "resources", source = "resources")
    PromptPublication toPromptPublication(PublicationDto dto, String path, List<PromptPublicationResource> resources);

    default FilePublication toFilePublication(PublicationDto dto, List<FilePublicationResource> resources) {
        var path = removePrefix(dto.getUrl(), PUBLICATIONS_PREFIX);
        return toFilePublication(dto, path, resources);
    }

    @Mapping(target = "requestName", source = "dto.name")
    @Mapping(target = "folderId", source = "dto.targetFolder")
    @Mapping(target = "resources", source = "resources")
    FilePublication toFilePublication(PublicationDto dto, String path, List<FilePublicationResource> resources);

    default ApplicationPublication toApplicationPublication(PublicationDto dto, List<ApplicationPublicationResource> resources, List<String> files) {
        var path = removePrefix(dto.getUrl(), PUBLICATIONS_PREFIX);
        return toApplicationPublication(dto, path, resources, files);
    }

    @Mapping(target = "requestName", source = "dto.name")
    @Mapping(target = "folderId", source = "dto.targetFolder")
    @Mapping(target = "resources", source = "resources")
    @Mapping(target = "files", source = "files")
    ApplicationPublication toApplicationPublication(PublicationDto dto, String path, List<ApplicationPublicationResource> resources, List<String> files);

    default ConversationPublication toConversationPublication(PublicationDto dto, List<ConversationPublicationResource> resources, List<String> files) {
        var path = removePrefix(dto.getUrl(), PUBLICATIONS_PREFIX);
        return toConversationPublication(dto, path, resources, files);
    }

    @Mapping(target = "requestName", source = "dto.name")
    @Mapping(target = "folderId", source = "dto.targetFolder")
    @Mapping(target = "resources", source = "resources")
    @Mapping(target = "files", source = "files")
    ConversationPublication toConversationPublication(PublicationDto dto, String path, List<ConversationPublicationResource> resources, List<String> files);

    default ToolSetPublication toToolSetPublication(PublicationDto dto, List<ToolSetPublicationResource> resources, List<String> files) {
        var path = removePrefix(dto.getUrl(), PUBLICATIONS_PREFIX);
        return toToolSetPublication(dto, path, resources, files);
    }

    @Mapping(target = "requestName", source = "dto.name")
    @Mapping(target = "folderId", source = "dto.targetFolder")
    @Mapping(target = "resources", source = "resources")
    @Mapping(target = "files", source = "files")
    ToolSetPublication toToolSetPublication(PublicationDto dto, String path, List<ToolSetPublicationResource> resources, List<String> files);

    @Mapping(target = "sourceUrl", ignore = true)
    @Mapping(target = "targetUrl", ignore = true)
    PromptPublicationResource toPromptPublicationResource(PublicationResourceActionDto action, Prompt prompt);

    @Mapping(target = "sourceUrl", ignore = true)
    @Mapping(target = "targetUrl", ignore = true)
    FilePublicationResource toFilePublicationResource(PublicationResourceActionDto action, FileNodeInfo file);

    @Mapping(target = "sourceUrl", ignore = true)
    @Mapping(target = "targetUrl", ignore = true)
    ApplicationPublicationResource toApplicationPublicationResource(PublicationResourceActionDto action, ApplicationResource applicationResource);

    @Mapping(target = "sourceUrl", ignore = true)
    @Mapping(target = "targetUrl", ignore = true)
    ConversationPublicationResource toConversationPublicationResource(PublicationResourceActionDto action, Conversation conversation);

    @Mapping(target = "sourceUrl", ignore = true)
    @Mapping(target = "targetUrl", ignore = true)
    ToolSetPublicationResource toToolSetPublicationResource(PublicationResourceActionDto action, ToolSetResource toolSetResource);

    private static String removePrefix(String path, String prefix) {
        if (path.startsWith(prefix)) {
            return path.substring(prefix.length());
        } else {
            throw new IllegalArgumentException("The string does not start with the specified prefix: '%s': %s"
                    .formatted(prefix, path));
        }
    }

    @Mapping(target = "targetFolder", source = "targetFolder", qualifiedByName = "encodeFolderPath")
    CreatePublicationDto toCreatePublicationDto(CreatePublication createPublication);

    @Mapping(target = "reviewUrl", ignore = true)
    PublicationResourceDto toPublicationResourceDto(PublicationResource publicationResource);

    default Map<String, List<Rule>> toRules(Map<String, List<RuleDto>> rules) {
        if (MapUtils.isEmpty(rules)) {
            return Collections.emptyMap();
        }
        return rules.entrySet().stream()
                .collect(Collectors.toMap(
                        entry -> decodePath(entry.getKey()),
                        entry -> toRules(entry.getValue())
                ));
    }

    List<Rule> toRules(List<RuleDto> rules);

    @Named("encodeFolderPath")
    default String encodeFolderPath(String path) {
        return CoreMetadataUtils.encodeFolderPath(path);
    }

    @Named("decodePath")
    default String decodePath(String path) {
        return CoreMetadataUtils.decodePath(path);
    }
}
