package com.epam.aidial.cfg.dao.model;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
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
public class InterceptorEntity extends AbstractEntity<String> {

    @Id
    @EqualsAndHashCode.Include
    private String name;
    private String endpoint;
    private String iconUrl;
    private String description;
    private String displayName;
    private Boolean forwardAuthToken;
    private String author;
    private Long createdAt;
    private Long updatedAt;
    private List<String> dependencies;
    private String configurationEndpoint;

    @ToString.Exclude
    private String containerId;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interceptor_runner_name")
    private InterceptorRunnerEntity interceptorRunner;

    @ToString.Exclude
    @ManyToMany
    @JoinTable(
            name = "interceptor_application",
            joinColumns = @JoinColumn(name = "interceptor_name"),
            inverseJoinColumns = @JoinColumn(name = "application_name")
    )
    private List<ApplicationEntity> applications = new ArrayList<>();

    @ToString.Exclude
    @ManyToMany
    @JoinTable(
            name = "interceptor_model",
            joinColumns = @JoinColumn(name = "interceptor_name"),
            inverseJoinColumns = @JoinColumn(name = "model_name")
    )
    private List<ModelEntity> models = new ArrayList<>();

    @PreRemove
    public void preRemove() {
        applications.forEach(application -> application.getInterceptors().remove(this));
        models.forEach(model -> model.getInterceptors().remove(this));
    }

    @Override
    public String getId() {
        return name;
    }
}
