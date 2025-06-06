package com.epam.aidial.cfg.dto;

import com.epam.aidial.cfg.web.validation.NoDotEndingInPathSegments;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromptEximDto {

    @NoDotEndingInPathSegments
    @Pattern(regexp = "prompts/public/([^/]+/)*[^/]+__[^/]+")
    private String id;
    private String name;
    private String folderId;
    private String description;
    private String content;

}
