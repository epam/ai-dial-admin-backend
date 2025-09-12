package com.epam.aidial.cfg.dto;

import com.epam.aidial.cfg.dto.validation.annotation.HttpMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Data
public class RouteResourceDto {
    @NotBlank(message = "Name is required")
    private String name;
    private List<String> userRoles;
    private ResponseDto response;
    private boolean rewritePath;
    private List<@NotNull String> paths;
    private Set<@HttpMethod String> methods;
    private List<UpstreamDto> upstreams;
    private int maxRetryAttempts;
    private Integer order;
    private Set<ResourceAccessType> permissions;
    private AttachmentPath attachmentPaths;

    @Data
    public static class AttachmentPath {
        private List<String> requestBody = new ArrayList<>();
        private List<String> responseBody = new ArrayList<>();
    }

    public enum ResourceAccessType {
        READ,
        WRITE,
        SHARE
    }
}
