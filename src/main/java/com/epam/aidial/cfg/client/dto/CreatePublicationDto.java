package com.epam.aidial.cfg.client.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor
public class CreatePublicationDto {

    private String targetFolder;
    @Builder.Default
    private List<PublicationResourceDto> resources = new ArrayList<>();
    private List<RuleDto> rules;
}
