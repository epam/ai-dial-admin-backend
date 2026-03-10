package com.epam.aidial.cfg.dao.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderColumn;
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
    private boolean appendApplicationPropertiesHeader = true;
    private String applicationTypeIconUrl;
    private Boolean applicationTypePlaybackSupport;
    @Enumerated(EnumType.STRING)
    private CopyAppBucketOptionsEntity applicationTypeBucketCopy;
    private boolean applicationTypeAssistantAttachmentsInRequestSupported;
    private String applicationTypeSchemaEndpoint;
    @Embedded
    private ApplicationTypeMcpEntity applicationTypeMcp;

    @ToString.Exclude
    @ManyToMany
    @JoinTable(
            name = "interceptor_application_type_schema",
            joinColumns = @JoinColumn(name = "application_type_schema_id"),
            inverseJoinColumns = @JoinColumn(name = "interceptor_name")
    )
    @OrderColumn
    private List<InterceptorEntity> interceptors = new ArrayList<>();

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

    public enum CopyAppBucketOptionsEntity {
        ENABLED,
        DISABLED,
    }
}