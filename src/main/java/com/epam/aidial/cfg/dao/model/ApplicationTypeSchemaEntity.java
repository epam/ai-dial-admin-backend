package com.epam.aidial.cfg.dao.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
    @Enumerated(EnumType.STRING)
    private TypeEntity type;
    private String title;
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
    private String applicationTypeIconUrl;
    private Boolean applicationTypePlaybackSupport;

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

    @Column(columnDefinition = "CLOB", name = "routes")
    private String routes;

    @Override
    public String getId() {
        return schemaId;
    }

    public enum TypeEntity {
        OBJECT,
        BOOLEAN,
    }
}
