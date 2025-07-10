package com.epam.aidial.cfg.mapper;

import com.epam.aidial.cfg.dto.FolderInfoDto;
import com.epam.aidial.cfg.dto.ImportResourcesDto;
import com.epam.aidial.cfg.dto.ImportResourcesFileResultDto;
import com.epam.aidial.cfg.dto.ImportResourcesPreviewDto;
import com.epam.aidial.cfg.dto.MoveResourceDto;
import com.epam.aidial.cfg.dto.ResourceMetadataRequestDto;
import com.epam.aidial.cfg.dto.RuleDto;
import com.epam.aidial.cfg.dto.UpdateRulesRequestDto;
import com.epam.aidial.cfg.model.FolderInfo;
import com.epam.aidial.cfg.model.ImportResources;
import com.epam.aidial.cfg.model.ImportResourcesFileResult;
import com.epam.aidial.cfg.model.ImportResourcesPreview;
import com.epam.aidial.cfg.model.MoveResource;
import com.epam.aidial.cfg.model.ResourceMetadataRequest;
import com.epam.aidial.cfg.model.Rule;
import com.epam.aidial.cfg.model.UpdateRulesRequest;
import org.mapstruct.Mapper;

import java.util.List;
import java.util.Map;

@Mapper(componentModel = "spring")
public interface ResourceMapper {

    MoveResource toMoveResource(MoveResourceDto movePromptDto);

    default ResourceMetadataRequest toRequest(ResourceMetadataRequestDto dto) {
        if (dto == null) {
            return ResourceMetadataRequest.builder()
                    .build();
        }

        return ResourceMetadataRequest.builder()
                .path(dto.getPath())
                .recursive(dto.isRecursive())
                .nextToken(dto.getNextToken())
                .limit(dto.getLimit())
                .build();
    }

    FolderInfoDto toFolderInfoDto(FolderInfo folderInfo);

    UpdateRulesRequest toUpdateRulesRequest(UpdateRulesRequestDto dto);

    Rule toRule(RuleDto dto);

    Map<String, List<RuleDto>> toRuleDtos(Map<String, List<Rule>> rules);

    List<RuleDto> toRuleDtos(List<Rule> rules);

    RuleDto toRuleDto(Rule rule);

    ImportResources toImportResources(ImportResourcesDto dto);

    ImportResourcesFileResultDto toImportResourcesFileResultDto(ImportResourcesFileResult model);

    ImportResourcesPreviewDto toImportResourcesPreviewDto(ImportResourcesPreview importResourcesPreview);
}
