package com.epam.aidial.cfg.domain.model;

import com.epam.aidial.cfg.domain.model.source.ModelSource;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class Model extends RoleBased {

    private String endpoint;
    private String description;
    private String displayName;
    private String displayVersion;
    private String iconUrl;
    private String reference;
    private Boolean forwardAuthToken;
    private Features features;
    private List<String> inputAttachmentTypes;
    private Integer maxInputAttachments;
    private Map<String, Object> defaults;
    private List<String> interceptors;
    private LinkedHashSet<String> topics; //todo: rename to descriptionKeywords
    private Integer maxRetryAttempts;
    private String author;
    private Long createdAt;
    private Long updatedAt;
    private List<String> dependencies;
    private ModelType type;
    private String tokenizerModel;
    private TokenLimits limits;
    private Pricing pricing;
    private List<Upstream> upstreams;
    private String overrideName;
    private List<String> fieldsHashingOrder;
    private ModelSource source;
}
