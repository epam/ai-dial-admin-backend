package com.epam.aidial.cfg.dao.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.PreRemove;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.envers.Audited;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Entity
@Audited
public class ModelEntity extends TimeTrackableEntity<String> {

    @Id
    @EqualsAndHashCode.Include
    private String deploymentName;

    @MapsId
    @JoinColumn(name = "deployment_name", unique = true)
    @OneToOne(targetEntity = DeploymentEntity.class, fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private DeploymentEntity deployment;
    private String description;
    private String displayName;
    private String displayVersion;
    private String iconUrl;
    private String reference;
    private Boolean forwardAuthToken;
    @Embedded
    private FeaturesEntity features;
    private List<String> inputAttachmentTypes;
    private Integer maxInputAttachments;
    @Column(columnDefinition = "CLOB")
    private String defaults;
    @ToString.Exclude
    @ManyToMany
    @JoinTable(
            name = "interceptor_model",
            joinColumns = @JoinColumn(name = "model_name"),
            inverseJoinColumns = @JoinColumn(name = "interceptor_name")
    )
    @OrderColumn
    private List<InterceptorEntity> interceptors = new ArrayList<>();
    private Set<String> topics;
    private int maxRetryAttempts = 1;
    private String author;
    private List<String> dependencies;
    private ModelTypeEntity type;
    private String tokenizerModel;
    @Embedded
    private TokenLimitsEntity limits;
    @Embedded
    private PricingEntity pricing;
    @Column(columnDefinition = "CLOB")
    private String upstreams;
    private String overrideName;
    private List<String> fieldsHashingOrder;
    private String endpoint;
    private String responsesEndpoint;

    @Embedded
    private ModelContainerEntity modelContainer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "adapter_name")
    private AdapterEntity adapter;
    private String adapterCompletionEndpointPath;

    @PreRemove
    public void preRemove() {
        for (InterceptorEntity interceptor : interceptors) {
            interceptor.getModels().remove(this);
        }
        if (adapter != null) {
            adapter.getModels().remove(this);
        }
    }

    @Override
    public String getId() {
        return deploymentName;
    }
}