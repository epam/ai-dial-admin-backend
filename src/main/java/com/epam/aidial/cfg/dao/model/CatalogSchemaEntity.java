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
import org.hibernate.envers.Audited;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Entity
@Audited
public class CatalogSchemaEntity extends TimeTrackableEntity<String> {

    @Id
    @EqualsAndHashCode.Include
    private String schemaId;

    @Column(columnDefinition = "CLOB")
    private String schema;

    @Enumerated(EnumType.STRING)
    private TypeEntity type;

    private String title;

    private String description;

    @Enumerated(EnumType.STRING)
    private CatalogEntityTypeEntity catalogEntityType;

    private String catalogDisplayName;

    private String defaultLocale;

    @Column(columnDefinition = "CLOB")
    private String defs;

    @Column(columnDefinition = "CLOB")
    private String properties;

    @Column(columnDefinition = "CLOB")
    private List<String> required;

    private Set<String> topics;

    @ToString.Exclude
    @OneToMany(mappedBy = "catalogSchema")
    private List<ApplicationEntity> applications = new ArrayList<>();

    @ToString.Exclude
    @OneToMany(mappedBy = "catalogSchema")
    private List<ModelEntity> models = new ArrayList<>();

    @ToString.Exclude
    @OneToMany(mappedBy = "catalogSchema")
    private List<ToolSetEntity> toolSets = new ArrayList<>();

    @Override
    public String getId() {
        return schemaId;
    }

    public enum TypeEntity {
        OBJECT,
        BOOLEAN
    }

    public enum CatalogEntityTypeEntity {
        MODEL,
        AGENT,
        TOOLSET,
        SKILL,
        INTERCEPTOR
    }
}
