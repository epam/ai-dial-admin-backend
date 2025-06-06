package com.epam.aidial.cfg.mapper;

import com.epam.aidial.cfg.dto.ApplicationResourceDto;
import com.epam.aidial.cfg.dto.ApplicationResourcePublicationDto;
import com.epam.aidial.cfg.dto.FileInfoDto;
import com.epam.aidial.cfg.dto.FilePublicationDto;
import com.epam.aidial.cfg.dto.PromptDto;
import com.epam.aidial.cfg.dto.PromptPublicationDto;
import com.epam.aidial.cfg.dto.PublicationDto;
import com.epam.aidial.cfg.dto.PublicationInfosDto;
import com.epam.aidial.cfg.dto.ResourceTypeDto;
import com.epam.aidial.cfg.model.ApplicationPublication;
import com.epam.aidial.cfg.model.ApplicationPublicationResource;
import com.epam.aidial.cfg.model.ApplicationResource;
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
import com.epam.aidial.metric.util.CollectorsUtils;
import org.mapstruct.Mapper;

import java.util.List;

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
            return toApplicationResourcePublicationDto(applicationPublication, action);
        }

        throw new IllegalArgumentException("Unsupported publication type: %s. Publication: %s"
                .formatted(model.getClass(), model));
    }

    default PromptPublicationDto toPromptPublicationDto(PromptPublication model, PublicationResourceAction action) {
        var prompts = model.getResources()
                .stream()
                .map(PromptPublicationResource::getPrompt)
                .map(this::toPromptDto)
                .toList();

        return toPromptPublicationDto(model, action, prompts);
    }

    PromptPublicationDto toPromptPublicationDto(PromptPublication model, PublicationResourceAction action, List<PromptDto> prompts);

    default FilePublicationDto toFilePublicationDto(FilePublication model, PublicationResourceAction action) {
        var files = model.getResources()
                .stream()
                .map(FilePublicationResource::getFile)
                .map(this::toFileInfoDto)
                .toList();

        return toFilePublicationDto(model, action, files);
    }

    FilePublicationDto toFilePublicationDto(FilePublication model, PublicationResourceAction action, List<FileInfoDto> files);

    default ApplicationResourcePublicationDto toApplicationResourcePublicationDto(ApplicationPublication model, PublicationResourceAction action) {
        var applicationResources = model.getResources()
                .stream()
                .map(ApplicationPublicationResource::getApplicationResource)
                .map(this::toApplicationResourceDto)
                .toList();

        return toApplicationResourcePublicationDto(model, action, applicationResources);
    }

    ApplicationResourcePublicationDto toApplicationResourcePublicationDto(ApplicationPublication model,
                                                                          PublicationResourceAction action,
                                                                          List<ApplicationResourceDto> applicationResources);

    private PublicationResourceAction getAction(Publication model) {
        return model.getResources()
                .stream()
                .map(PublicationResource::getAction)
                .distinct()
                .collect(CollectorsUtils.toSingleton(()
                        -> new IllegalStateException("Different actions found inside publication request. Publication: %s".formatted(model))))
                .orElseThrow(() -> new IllegalStateException("No actions found inside publication request. Publication: %s".formatted(model)));
    }

    PromptDto toPromptDto(Prompt model);

    FileInfoDto toFileInfoDto(FileNodeInfo model);

    ApplicationResourceDto toApplicationResourceDto(ApplicationResource model);

    ResourceType toResourceType(ResourceTypeDto dto);

}
