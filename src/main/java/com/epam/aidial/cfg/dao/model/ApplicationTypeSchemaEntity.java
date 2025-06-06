package com.epam.aidial.cfg.dao.model;

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
public class ApplicationTypeSchemaEntity extends AbstractEntity<String> {

    @Id
    @EqualsAndHashCode.Include
    private String schemaId;

    private String schema;
    private String description;
    private String applicationTypeEditorUrl;
    private String applicationTypeViewerUrl;
    private String applicationTypeDisplayName;
    private String applicationTypeCompletionEndpoint;

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

    @Override
    public String getId() {
        return schemaId;
    }
}
