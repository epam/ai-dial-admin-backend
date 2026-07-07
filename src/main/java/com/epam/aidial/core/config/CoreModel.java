package com.epam.aidial.core.config;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Map;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class CoreModel extends Deployment {
    private ModelType type;
    private String tokenizerModel;
    private TokenLimits limits;
    private Pricing pricing;
    private List<CoreUpstream> upstreams = List.of();
    // if it's set then the model name is overridden with that name in the request body to the model adapter
    private String overrideName;

    @JsonAlias({"fieldsHashingOrder", "fields_hashing_order"})
    private List<String> fieldsHashingOrder = List.of("prefix.body.tools", "prefix.body.messages"); // 0.26.0

    @JsonAlias({"embeddingDimensions", "embedding_dimensions"})
    private Integer embeddingDimensions;

    /**
     * Supported LLM API interfaces keyed by interface type. Peer of endpoint/responsesEndpoint.
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Map<String, CoreDeploymentInterface> interfaces; // 0.46.0

    public CoreModel() {
        setMaxRetryAttempts(5);
    }

    @JsonIgnore
    public static CoreModel empty() {
        CoreModel coreModel = new CoreModel();

        coreModel.setUpstreams(null);
        coreModel.setFieldsHashingOrder(null);
        coreModel.setForwardAuthToken(null);
        coreModel.setDefaults(null);
        coreModel.setResponsesDefaults(null);
        coreModel.setInterceptors(null);
        coreModel.setDescriptionKeywords(null);
        coreModel.setMaxRetryAttempts(null);
        coreModel.setDependencies(null);
        coreModel.setInterfaces(null);

        return coreModel;
    }
}