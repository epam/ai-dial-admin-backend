package com.epam.aidial.cfg.model;

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
public class CreatePublication {

    private String targetFolder;
    @Builder.Default
    private List<PublicationResource> resources = new ArrayList<>();
    private List<Rule> rules;
}
