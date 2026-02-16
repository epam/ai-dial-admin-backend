package com.epam.aidial.cfg.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "$type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = FilePublicationDto.class, name = "file"),
        @JsonSubTypes.Type(value = ConversationPublicationDto.class, name = "conversation"),
        @JsonSubTypes.Type(value = ApplicationPublicationDto.class, name = "application"),
        @JsonSubTypes.Type(value = PromptPublicationDto.class, name = "prompt"),
        @JsonSubTypes.Type(value = ToolSetResourcePublicationDto.class, name = "toolset"),
})
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class PublicationDto {

    private String path;
    private String requestName;
    private String author;
    private String displayAuthor;
    private long createdAt;
    private PublicationStatusDto status;
    private String folderId;
    private PublicationResourceActionDto action;
    private List<RuleDto> rules;
    private List<PublicationResourceIssueDto> resourceIssues;

}