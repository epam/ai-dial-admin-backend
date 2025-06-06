package com.epam.aidial.cfg.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreatePromptDto {

    @NotNull
    private String name;
    @NotNull
    private String version;
    @NotNull
    private String folderId;
    private String description;
    @NotNull
    private String content;

}
