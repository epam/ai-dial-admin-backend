package com.epam.aidial.cfg.dao.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
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
public class InterceptorEntity extends TimeTrackableEntity<String> {

    @Id
    @EqualsAndHashCode.Include
    private String name;
    private String endpoint;
    private String responsesEndpoint;
    private String iconUrl;
    private String description;
    private String displayName;
    private Boolean forwardAuthToken;
    private String author;
    private List<String> dependencies;
    private Set<String> topics;

    @Column(columnDefinition = "CLOB")
    private String defaults;

    @Embedded
    private FeaturesEntity features;

    @Embedded
    private InterceptorContainerEntity interceptorContainer;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interceptor_runner_name")
    private InterceptorRunnerEntity interceptorRunner;

    @ToString.Exclude
    @ManyToMany(mappedBy = "interceptors")
    private List<ApplicationEntity> applications = new ArrayList<>();

    @ToString.Exclude
    @ManyToMany(mappedBy = "interceptors")
    private List<ModelEntity> models = new ArrayList<>();

    @ToString.Exclude
    @ManyToMany(mappedBy = "interceptors")
    private List<ApplicationTypeSchemaEntity> applicationTypeSchemas = new ArrayList<>();

    @PreRemove
    public void preRemove() {
        applications.forEach(application -> application.getInterceptors().remove(this));
        models.forEach(model -> model.getInterceptors().remove(this));
        applicationTypeSchemas.forEach(applicationTypeSchema -> applicationTypeSchema.getInterceptors().remove(this));
        if (interceptorRunner != null) {
            interceptorRunner.getInterceptors().remove(this);
        }
    }

    @Override
    public String getId() {
        return name;
    }
}