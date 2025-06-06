package com.epam.aidial.cfg.mapper;

import com.epam.aidial.cfg.dto.CreatePromptDto;
import com.epam.aidial.cfg.dto.PromptDto;
import com.epam.aidial.cfg.dto.PromptNodeInfoDto;
import com.epam.aidial.cfg.dto.PromptVersionsDto;
import com.epam.aidial.cfg.dto.PromptsEximDto;
import com.epam.aidial.cfg.model.CreatePrompt;
import com.epam.aidial.cfg.model.Prompt;
import com.epam.aidial.cfg.model.PromptNodeInfo;
import com.epam.aidial.cfg.model.PromptsExim;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PromptMapper {

    PromptNodeInfoDto toPromptInfoDto(PromptNodeInfo model);

    List<PromptNodeInfoDto> toPromptInfoDto(List<PromptNodeInfo> models);

    PromptDto toPromptDto(Prompt model);

    default PromptVersionsDto toPromptVersionsDto(List<PromptNodeInfo> models) {
        if (models == null || models.isEmpty()) {
            return null;
        }

        var promptVersionsDto = new PromptVersionsDto();
        promptVersionsDto.setPrompts(toPromptInfoDto(models));
        return promptVersionsDto;
    }

    CreatePrompt toCreatePrompt(CreatePromptDto dto);

    PromptsEximDto toPromptsEximDto(PromptsExim model);

}
