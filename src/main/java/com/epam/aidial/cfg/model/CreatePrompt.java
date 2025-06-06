package com.epam.aidial.cfg.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePrompt {

    private String name;
    private String version;
    private String folderId;
    private String description;
    private String content;

}
