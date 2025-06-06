package com.epam.aidial.cfg.dto;

import lombok.Data;

import java.util.List;

@Data
public class PromptVersionsDto {

    private List<PromptNodeInfoDto> prompts;

}
