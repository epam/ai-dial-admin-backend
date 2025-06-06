package com.epam.aidial.cfg.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
public class ApplicationDto extends RoleBasedDto {

    @NotBlank(message = "Name is required")
    private String name;
    private String endpoint;
    private String displayName;
    private String displayVersion;
    private String iconUrl;
    private String description;
    private String reference;
    private Boolean forwardAuthToken;
    private FeaturesDto features = new FeaturesDto();
    private List<String> inputAttachmentTypes;
    private Integer maxInputAttachments;
    private Map<String, Object> defaults;
    private List<String> interceptors;
    private List<String> topics;
    private Integer maxRetryAttempts;
    private String author;
    private Long createdAt;
    private Long updatedAt;
    private List<String> dependencies;

    private String viewerUrl;
    private String editorUrl;

    private FunctionDto function;

    @JsonIgnore
    private Map<String, Object> applicationProperties = new HashMap<>();

    @JsonAnySetter
    public void setApplicationProperty(String key, Object value) {
        applicationProperties.put(key, value);
    }

    @JsonAnyGetter
    public Map<String, Object> getApplicationProperties() {
        return applicationProperties;
    }

    private URI customAppSchemaId;

    public void setFunction(FunctionDto function) {
        if (function != null) {
            throw new UnsupportedOperationException("application function is not supported");
        }
        this.function = function;
    }

    @Data
    public static class FunctionDto {

        private String id;
        private String runtime;
        private String authorBucket;
        private String sourceFolder;
        private String targetFolder;
        private StatusDto status;
        private String error;
        private MappingDto mapping;
        private Map<String, String> env;

        public enum StatusDto {
            @JsonAlias("STARTING")
            DEPLOYING,
            @JsonAlias("STOPPING")
            UNDEPLOYING,
            @JsonAlias("STARTED")
            DEPLOYED,
            @JsonAlias({"CREATED", "STOPPED"})
            UNDEPLOYED,
            FAILED,
        }

        @Data
        public static class MappingDto {
            @JsonAlias("completion")
            private String chatCompletion;
            private String rate;
            private String tokenize;
            private String truncatePrompt;
            private String configuration;
        }
    }

}
