package com.epam.aidial.cfg.dao.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PreRemove;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.envers.Audited;

import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Entity
@Audited
public class ApplicationEntity extends TimeTrackableEntity<String> {

    @Id
    @EqualsAndHashCode.Include
    private String deploymentName;

    @MapsId
    @JoinColumn(name = "deployment_name", unique = true)
    @OneToOne(targetEntity = DeploymentEntity.class, fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private DeploymentEntity deployment;

    private String endpoint;
    private String iconUrl;
    private String reference;
    private String description;
    private String displayName;
    private String displayVersion;
    private List<String> inputAttachmentTypes;
    private Integer maxInputAttachments;
    private Boolean forwardAuthToken;
    private List<String> descriptionKeywords;
    private Integer maxRetryAttempts;
    @Column(columnDefinition = "CLOB")
    private String defaults;
    @ToString.Exclude
    @ManyToMany(mappedBy = "applications")
    private List<InterceptorEntity> interceptors = new ArrayList<>();
    private String author;
    private List<String> dependencies;
    @Embedded
    private FeaturesEntity features;
    private String applicationProperties;
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_type_schema_id")
    private ApplicationTypeSchemaEntity applicationTypeSchema;
    private String viewerUrl;
    private String editorUrl;

    // TODO [VPA]: on delete, remove these routes only if they are not connected to application type schema
    @ToString.Exclude
    @OneToMany(mappedBy = "application", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RouteEntity> routes = new ArrayList<>();

    @PreRemove
    public void preRemove() {
        for (InterceptorEntity interceptor : interceptors) {
            interceptor.getApplications().remove(this);
        }
    }

    @Override
    public String getId() {
        return deploymentName;
    }
}
