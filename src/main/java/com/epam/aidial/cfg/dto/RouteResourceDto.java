package com.epam.aidial.cfg.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Data
public class RouteResourceDto {
    @NotBlank(message = "Name is required")
    private String name;
    private List<String> userRoles;
    private ResponseResourceDto response;
    private Boolean rewritePath;
    private List<String> paths;
    private Set<String> methods;
    private List<UpstreamResourceDto> upstreams;
    private Integer maxRetryAttempts;
    private Integer order;
    private Set<ResourceAccessType> permissions;
    private AttachmentPath attachmentPaths;

    @Data
    public static class AttachmentPath {
        private List<String> requestBody = new ArrayList<>();
        private List<String> responseBody = new ArrayList<>();
    }

    @Data
    public static class ResponseResourceDto {
        private int status;
        private String body;

    }

    public enum ResourceAccessType {
        READ,
        WRITE,
        SHARE
    }
}
