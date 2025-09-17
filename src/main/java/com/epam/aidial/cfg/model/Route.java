package com.epam.aidial.cfg.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Route {
    private String name;
    private List<String> userRoles;
    private Boolean rewritePath;
    private List<String> paths;
    private Set<String> methods;
    private List<UpstreamResource> upstreams;
    private Integer maxRetryAttempts;
    private Integer order;
    private Response response;
    @Builder.Default
    private Set<ResourceAccessType> permissions = Set.of();
    @Builder.Default
    private AttachmentPath attachmentPaths = new AttachmentPath();

    @Data
    public static class Response {
        private int status;
        private String body;
    }

    public enum ResourceAccessType {
        READ,
        WRITE,
        SHARE
    }
}
