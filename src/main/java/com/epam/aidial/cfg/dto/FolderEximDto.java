package com.epam.aidial.cfg.dto;

import com.epam.aidial.cfg.web.validation.NoDotEndingInPathSegments;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class FolderEximDto {

    @NoDotEndingInPathSegments
    @Pattern(regexp = "prompts/public/.+")
    private String id;
    private String name;
    private String folderId;
    private String type;

}
