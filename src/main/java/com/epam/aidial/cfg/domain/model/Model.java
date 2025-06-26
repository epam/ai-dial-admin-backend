package com.epam.aidial.cfg.domain.model;

import com.epam.aidial.cfg.dao.model.FeaturesEntity;
import com.epam.aidial.cfg.dao.model.PricingEntity;
import com.epam.aidial.cfg.dao.model.TokenLimitsEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;
import java.util.Map;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class Model extends RoleBased {

    private String description;
    private Adapter adapter;
    private String displayName;
    private String displayVersion;
    private String iconUrl;
    private String reference;
    private Boolean forwardAuthToken;
    private FeaturesEntity features;
    private List<String> inputAttachmentTypes;
    private Integer maxInputAttachments;
    private Map<String, Object> defaults;
    private List<String> interceptors;
    private List<String> topics; //todo: rename to descriptionKeywords
    private Integer maxRetryAttempts;
    private String author;
    private Long createdAt;
    private Long updatedAt;
    private List<String> dependencies;
    private ModelType type;
    private String tokenizerModel;
    private TokenLimitsEntity limits;
    private PricingEntity pricing;
    private List<Upstream> upstreams;
    private String overrideName;
    private List<String> fieldsHashingOrder;
}
