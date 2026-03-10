package com.epam.aidial.core.config;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.net.URI;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CoreApplication extends Deployment {

    private Function function;

    @JsonAlias({"applicationProperties", "application_properties"})
    private Map<String, Object> applicationProperties; //all custom application properties will land there

    @JsonAlias({"applicationTypeSchemaId", "application_type_schema_id"})
    private URI applicationTypeSchemaId;

    @JsonAlias({"viewerUrl", "viewer_url"})
    private String viewerUrl; // 0.29.0
    @JsonAlias({"editorUrl", "editor_url"})
    private String editorUrl; // 0.29.0

    private Mcp mcp;  // 0.42.0

    // maintain the order of routes defined in the app config
    private LinkedHashMap<String, CoreRoute> routes = new LinkedHashMap<>(); // 0.32.0

    @Data
    @Accessors(chain = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class Function {

        private String id;
        private String runtime;
        private String authorBucket;
        private String sourceFolder;
        private String targetFolder;
        private Status status;
        private String error;
        private Mapping mapping;
        private Map<String, String> env;

        public enum Status {
            @JsonAlias("STARTING")
            DEPLOYING,
            @JsonAlias("STOPPING")
            UNDEPLOYING,
            @JsonAlias("STARTED")
            DEPLOYED,
            @JsonAlias({"CREATED", "STOPPED"})
            UNDEPLOYED,
            FAILED;

            public boolean isPending() {
                return switch (this) {
                    case DEPLOYED, FAILED, UNDEPLOYED -> false;
                    case DEPLOYING, UNDEPLOYING -> true;
                };
            }

            public boolean isActive() {
                return switch (this) {
                    case FAILED, UNDEPLOYED -> false;
                    case DEPLOYING, DEPLOYED, UNDEPLOYING -> true;
                };
            }
        }

        @Data
        @Accessors(chain = true)
        @JsonInclude(JsonInclude.Include.NON_NULL)
        @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
        public static class Mapping {
            @JsonAlias("completion")
            private String chatCompletion;
            private String rate;
            private String tokenize;
            private String truncatePrompt;
            private String configuration;
        }
    }

    @Data
    @Accessors(chain = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Logs {
        private Collection<Log> logs;
    }

    @Data
    @Accessors(chain = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class Log {
        private String instance;
        private String content;
    }

    @Data
    public static class Mcp {
        private String endpoint;
        private final Transport transport = Transport.HTTP;
        @JsonAlias({"allowedTools", "allowed_tools"})
        private List<String> allowedTools;

        public enum Transport {
            HTTP
        }
    }

    public CoreApplication() {
        super();
    }
}