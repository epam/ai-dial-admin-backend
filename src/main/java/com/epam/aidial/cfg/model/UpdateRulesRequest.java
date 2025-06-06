package com.epam.aidial.cfg.model;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateRulesRequest {

    @NotEmpty
    private String targetFolder;
    @NotNull
    private List<Rule> rules;
}
