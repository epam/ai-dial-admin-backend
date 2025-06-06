package com.epam.aidial.core.config;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

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

    public CoreModel() {
        setMaxRetryAttempts(5);
    }
}