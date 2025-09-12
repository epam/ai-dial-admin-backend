package com.epam.aidial.cfg.client.dto;

import com.epam.aidial.cfg.dto.validation.annotation.HttpMethod;
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
public class RouteDto {

    private String name;
    private List<String> userRoles;
    private ResponseDto response;
    private Boolean rewritePath;
    private List<String> paths;
    private Set<@HttpMethod String> methods;
    private List<UpstreamDto> upstreams;
    private Integer maxRetryAttempts;
    private Integer order;
    private Set<ResourceAccessType> permissions;
    private AttachmentPath attachmentPaths;

    @Data
    public static class Response {
        private int status;
        private String body;
    }

    @Data
    public static class AttachmentPath {
        private List<String> requestBody = List.of();
        private List<String> responseBody = List.of();
    }

    public enum ResourceAccessType {
        READ,
        WRITE,
        SHARE;
    }
}
