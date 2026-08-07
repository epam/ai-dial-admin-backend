package com.epam.aidial.cfg.dao.model;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
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
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(
                    name = "plainValue",
                    column = @Column(name = "description")),
            @AttributeOverride(
                    name = "localeMap",
                    column = @Column(name = "description_i18n"))
    })
    private LocalizedValueEntity description;
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(
                    name = "plainValue",
                    column = @Column(name = "intro")),
            @AttributeOverride(
                    name = "localeMap",
                    column = @Column(name = "intro_i18n"))
    })
    private LocalizedValueEntity intro;
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(
                    name = "plainValue",
                    column = @Column(name = "display_name")),
            @AttributeOverride(
                    name = "localeMap",
                    column = @Column(name = "display_name_i18n"))
    })
    private LocalizedValueEntity displayName;
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

    private String catalogSchemaId;

    @Column(columnDefinition = "CLOB")
    private String catalogProperties;

    public enum TransportEntity {
        HTTP, SSE
    }

    @NotNull
    @Override
    public String getId() {
        return deploymentName;
    }
}