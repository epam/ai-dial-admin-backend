package com.epam.aidial.core.config;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

@Data
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CoreRoute extends RoleBasedEntity {

    public static final AttachmentPath EMPTY_ATTACHMENT_PATHS = new AttachmentPath();

    @JsonAlias({"response", "dial:response"})
    private Response response;
    @JsonAlias({"rewritePath", "dial:rewritePath"})
    private Boolean rewritePath = false;
    @JsonAlias({"paths", "dial:paths"})
    private List<Pattern> paths = List.of();
    @JsonAlias({"methods", "dial:methods"})
    private Set<String> methods = Set.of();
    @JsonAlias({"upstreams", "dial:upstreams"})
    private List<CoreUpstream> upstreams = List.of();
    /**
     * Indicated max retry attempts to route a single user request.
     */
    @JsonAlias({"maxRetryAttempts", "dial:maxRetryAttempts"})
    private Integer maxRetryAttempts = 1;
    /**
     * Determines the order the route is resolved. The lower value means the higher priority.
     */
    @Min(value = 0, message = "Order can't be negative")
    @JsonAlias({"order", "dial:order"})
    private Integer order = Integer.MAX_VALUE; // 0.32.0
    @JsonAlias({"permissions", "dial:permissions"})
    private Set<ResourceAccessType> permissions = Set.of(); // 0.32.0
    @JsonAlias({"attachmentPaths", "dial:attachmentPaths"})
    private AttachmentPath attachmentPaths = EMPTY_ATTACHMENT_PATHS; // 0.32.0

    @Data
    public static class Response {
        private int status = 200;
        private String body = "";
    }

    public enum ResourceAccessType {
        @JsonAlias({"read", "READ"})
        @JsonProperty("READ")
        READ,
        @JsonAlias({"write", "WRITE"})
        @JsonProperty("WRITE")
        WRITE
    }

    /**
     * The class describes metadata where Core needs to find attachments for auto-sharing.
     */
    @Data
    public static class AttachmentPath {
        /**
         * List of JSON paths in the HTTP request body.
         */
        private List<String> requestBody = List.of();

        /**
         * List of JSON paths in the HTTP response body.
         */
        private List<String> responseBody = List.of();
    }

    @JsonIgnore
    public static CoreRoute empty() {
        CoreRoute coreRoute = new CoreRoute();

        coreRoute.setResponse(null);
        coreRoute.setPaths(null);
        coreRoute.setMethods(null);
        coreRoute.setUpstreams(null);
        coreRoute.setMaxRetryAttempts(null);
        coreRoute.setOrder(null);
        coreRoute.setPermissions(null);
        coreRoute.setAttachmentPaths(null);
        coreRoute.setName(null);
        coreRoute.setUserRoles(null);
        coreRoute.setRewritePath(null);
        return coreRoute;
    }
}