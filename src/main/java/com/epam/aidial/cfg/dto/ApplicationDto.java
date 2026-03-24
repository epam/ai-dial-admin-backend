package com.epam.aidial.cfg.dto;

import com.epam.aidial.cfg.dto.route.DependentRouteDto;
import com.epam.aidial.cfg.dto.validation.annotation.Endpoint;
import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.net.URI;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

@Data
@EqualsAndHashCode(callSuper = true)
public class ApplicationDto extends RoleBasedDto {

    @NotBlank(message = "Name is required")
    private String name;
    @Endpoint
    private String endpoint;
    @NotBlank(message = "DisplayName is required")
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
    private TreeSet<String> topics;
    @Positive(message = "Max retry attempts should be greater than 0")
    private Integer maxRetryAttempts;
    private String author;
    @EqualsAndHashCode.Exclude
    private Instant createdAt;
    @EqualsAndHashCode.Exclude
    private Instant updatedAt;
    private List<String> dependencies;

    private String viewerUrl;
    private String editorUrl;

    private List<DependentRouteDto> routes;

    private FunctionDto function;

    private Map<String, Object> applicationProperties = new HashMap<>();

    private URI customAppSchemaId;
    private ValidityStateDto validityState;
    private McpDto mcp;

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