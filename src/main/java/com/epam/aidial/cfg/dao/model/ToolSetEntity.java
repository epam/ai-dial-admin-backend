package com.epam.aidial.cfg.dao.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PreRemove;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
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
    private String intro;
    private String displayName;
    private String vendorWebsite;
    private Set<String> descriptionKeywords;
    private int maxRetryAttempts = 1;
    private String author;
    private boolean forwardAuthToken;

    @Embedded
    private ToolSetContainerEntity toolSetContainer;

    @Embedded
    private ToolSetMcpRegistryEntity toolSetMcpRegistry;

    @Enumerated(EnumType.STRING)
    private TransportEntity transport;

    private List<String> allowedTools;

    private String provider;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "catalog_schema_id")
    private CatalogSchemaEntity catalogSchema;

    @Column(columnDefinition = "CLOB")
    private String catalogProperties;

    @PreRemove
    public void preRemove() {
        if (catalogSchema != null) {
            catalogSchema.getToolSets().remove(this);
        }
    }

    public enum TransportEntity {
        HTTP, SSE
    }

    @NotNull
    @Override
    public String getId() {
        return deploymentName;
    }
}