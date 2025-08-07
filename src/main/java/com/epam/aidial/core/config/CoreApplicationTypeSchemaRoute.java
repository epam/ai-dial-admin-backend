package com.epam.aidial.core.config;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import com.epam.aidial.core.config.CoreRoute.ResourceAccessType;

/**
    This class conforms to application type schema's meta schema. Main class for reference is CoreRoute.
    @see com.epam.aidial.core.config.CoreRoute
 */
@Data
public class CoreApplicationTypeSchemaRoute {

    @JsonAlias({"userRoles", "user_roles", "dial:userRoles"})
    @JsonProperty("dial:userRoles")
    private Set<String> userRoles;

    @JsonAlias({"response", "dial:response"})
    @JsonProperty("dial:response")
    private Response response;

    @JsonAlias({"rewritePath", "dial:rewritePath"})
    @JsonProperty("dial:rewritePath")
    private boolean rewritePath;

    @JsonAlias({"paths", "dial:paths"})
    @JsonProperty("dial:paths")
    private List<Pattern> paths = List.of();

    @JsonAlias({"methods", "dial:methods"})
    @JsonProperty("dial:methods")
    private Set<String> methods = Set.of();

    @JsonAlias({"upstreams", "dial:upstreams"})
    @JsonProperty("dial:upstreams")
    private List<CoreApplicationTypeSchemaUpstream> upstreams = List.of();

    @Min(value = 0, message = "Order can't be negative")
    @JsonAlias({"order", "dial:order"})
    @JsonProperty("dial:order")
    private int order = Integer.MAX_VALUE; // 0.32.0

    @JsonAlias({"permissions", "dial:permissions"})
    @JsonProperty("dial:permissions")
    private Set<ResourceAccessType> permissions = Set.of(); // 0.32.0

    @JsonAlias({"attachmentPaths", "dial:attachmentPaths"})
    @JsonProperty("dial:attachmentPaths")
    private AttachmentPath attachmentPaths = new AttachmentPath(); // 0.32.0

    @Data
    public static class Response {
        @JsonProperty("dial:status")
        private int status = 200;
        @JsonProperty("dial:body")
        private String body = "";
    }

    @Data
    public static class AttachmentPath {
        @JsonProperty("dial:requestBody")
        private List<String> requestBody = List.of();
        @JsonProperty("dial:responseBody")
        private List<String> responseBody = List.of();
    }
}
