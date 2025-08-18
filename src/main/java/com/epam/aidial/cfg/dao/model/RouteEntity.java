package com.epam.aidial.cfg.dao.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.envers.Audited;

import java.util.List;
import java.util.Set;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Entity
@Audited
public class RouteEntity extends TimeTrackableEntity<String> {

    @Id
    @EqualsAndHashCode.Include
    private String deploymentName;

    @MapsId
    @JoinColumn(name = "deployment_name", unique = true)
    @OneToOne(targetEntity = DeploymentEntity.class, fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private DeploymentEntity deployment;

    private String description;
    @Embedded
    private ResponseEntity response;
    private boolean rewritePath;
    private List<String> paths;
    private Set<String> methods;
    private String upstreams;
    private int maxRetryAttempts;
    @Column(name = "order_value")
    private int order;

    @Override
    public String getId() {
        return deploymentName;
    }
}
