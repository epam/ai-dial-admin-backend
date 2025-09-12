package com.epam.aidial.cfg.model;

import com.epam.aidial.cfg.dto.validation.annotation.HttpMethod;
import jakarta.validation.constraints.NotNull;
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
    private List<@NotNull String> paths;
    private Set<@HttpMethod String> methods;
    private List<Upstream> upstreams;
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
