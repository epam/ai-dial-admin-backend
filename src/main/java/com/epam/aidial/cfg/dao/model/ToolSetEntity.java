package com.epam.aidial.cfg.dao.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.envers.Audited;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Entity
@Audited
public class ToolSetEntity extends TimeTrackableEntity<String> {

    @Id
    @EqualsAndHashCode.Include
    private String deploymentName;

    @MapsId
    @JoinColumn(name = "deployment_name", unique = true)
    @OneToOne(targetEntity = SecuredResourceEntity.class, fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private SecuredResourceEntity deployment;

    private String endpoint;
    private String iconUrl;
    private String description;
    private String displayName;
    private Set<String> descriptionKeywords;
    private int maxRetryAttempts = 1;
    private String author;

    @Embedded
    private ToolSetContainerEntity toolSetContainer;

    @Enumerated(EnumType.STRING)
    private TransportEntity transport;

    private List<String> allowedTools;

    public enum TransportEntity {
        HTTP, SSE
    }

    @NotNull
    @Override
    public String getId() {
        return deploymentName;
    }
}