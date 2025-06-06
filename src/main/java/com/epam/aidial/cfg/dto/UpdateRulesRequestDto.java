package com.epam.aidial.cfg.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class UpdateRulesRequestDto {

    @NotEmpty
    private String targetFolder;
    @NotNull
    private List<RuleDto> rules;
}
