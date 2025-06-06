package com.epam.aidial.cfg.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RuleDto {

    @NotEmpty
    private String source;
    private RuleFunctionDto function;
    @NotEmpty
    private List<String> targets;
}
