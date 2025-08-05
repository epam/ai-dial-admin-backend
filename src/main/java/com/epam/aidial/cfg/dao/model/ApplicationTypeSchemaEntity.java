package com.epam.aidial.cfg.dao.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.envers.AuditJoinTable;
import org.hibernate.envers.Audited;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Entity
@Audited
public class ApplicationTypeSchemaEntity extends TimeTrackableEntity<String> {

    @Id
    @EqualsAndHashCode.Include
    private String schemaId;

    private String schema;
    private String description;
    private String applicationTypeEditorUrl;
    private String applicationTypeViewerUrl;
    private String applicationTypeDisplayName;
    private String applicationTypeCompletionEndpoint;
    private String applicationTypeConfigurationEndpoint;
    private String applicationTypeRateEndpoint;
    private String applicationTypeTokenizeEndpoint;
    private String applicationTypeTruncatePromptEndpoint;
    private Boolean appendApplicationPropertiesHeader;

    @Column(columnDefinition = "CLOB")
    private String defs;
    @Column(columnDefinition = "CLOB")
    private String properties;

    private List<String> required;

    @ToString.Exclude
    @OneToMany(mappedBy = "applicationTypeSchema")
    @AuditJoinTable
    private List<ApplicationEntity> applications = new ArrayList<>();

    private Set<String> topics;

    // TODO [VPA]: on delete, remove these routes only if they are not connected to application
    @ToString.Exclude
    @OneToMany(mappedBy = "application", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RouteEntity> applicationTypeRoutes = new ArrayList<>();

    @Override
    public String getId() {
        return schemaId;
    }
}
