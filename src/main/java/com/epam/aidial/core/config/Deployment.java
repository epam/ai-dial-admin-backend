package com.epam.aidial.core.config;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
public abstract class Deployment extends RoleBasedEntity {
    private String endpoint;
    @JsonAlias({"responsesEndpoint", "responses_endpoint"})
    private String responsesEndpoint;  //0.42.0
    @JsonAlias({"displayName", "display_name"})
    private String displayName;
    @JsonAlias({"displayVersion", "display_version"})
    private String displayVersion;
    @JsonAlias({"iconUrl", "icon_url"})
    private String iconUrl;
    private String description;
    private String reference;
    private String intro; //0.46.0
    /**
     * Forward Http header with authorization token when request is sent to deployment.
     * Authorization token is NOT forwarded by default.
     */
    @JsonAlias({"forwardAuthToken", "forward_auth_token"})
    private Boolean forwardAuthToken = false;
    private CoreFeatures features;
    @JsonAlias({"inputAttachmentTypes", "input_attachment_types"})
    private List<String> inputAttachmentTypes;
    @JsonAlias({"maxInputAttachments", "max_input_attachments"})
    private Integer maxInputAttachments;
    /**
     * Default parameters are applied if a request doesn't contain them in OpenAI chat/completions API call.
     */
    private Map<String, Object> defaults = new HashMap<>();
    /**
     * Default parameters are applied if a request doesn't contain them in OpenAI Responses API call.
     */
    @JsonAlias({"responses_defaults", "responsesDefaults"})
    private Map<String, Object> responsesDefaults = Map.of(); //0.43.0
    /**
     * Supported LLM API interfaces keyed by interface-type value. Peer of endpoint/responsesEndpoint.
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Map<String, CoreDeploymentInterface> interfaces = Map.of(); //0.46.0
    /**
     * List of interceptors to be called for the deployment
     */
    private List<String> interceptors = new ArrayList<>();
    /**
     * The field contains a list of keywords aka tags which describe the deployment, e.g. code-gen, text2image.
     */
    @JsonAlias({"descriptionKeywords", "description_keywords"})
    private List<String> descriptionKeywords = new ArrayList<>();

    /**
     * Indicated max retry attempts to route a single user request.
     */
    @JsonAlias({"maxRetryAttempts", "max_retry_attempts"})
    private Integer maxRetryAttempts = 1;

    /**
     * The author who has developed that deployment(application/assistant/model)
     */
    private String author; // 0.24.0

    @JsonAlias({"createdAt", "created_at"})
    private Long createdAt; // 0.25.0

    @JsonAlias({"updatedAt", "updated_at"})
    private Long updatedAt; // 0.25.0

    /**
     * Dependent deployments
     */
    private List<String> dependencies = List.of(); // 0.27.0

    /**
     * Catalog schema reference for marketplace/catalog display metadata validation.
     */
    @JsonAlias({"catalogSchemaId", "catalog_schema_id"})
    private URI catalogSchemaId; // 0.47.0

    /**
     * Curated marketplace/catalog display metadata, validated against {@link #catalogSchemaId}.
     */
    @JsonAlias({"catalogProperties", "catalog_properties"})
    private Map<String, Object> catalogProperties; // 0.47.0

    /**
     * If it's set then the deployment name is overridden with that name in the request body to the adapter.
     */
    @JsonAlias({"overrideName", "override_name"})
    private String overrideName; // 0.47.0

}