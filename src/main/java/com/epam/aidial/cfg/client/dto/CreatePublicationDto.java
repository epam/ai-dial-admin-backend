package com.epam.aidial.cfg.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreatePublicationDto {

    private String targetFolder;
    @Builder.Default
    private List<PublicationResourceDto> resources = new ArrayList<>();
    private List<RuleDto> rules;
}
