package com.epam.aidial.cfg.dao.model;

import jakarta.persistence.Embedded;
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
public class AdapterEntity extends TimeTrackableEntity<String> {

    @Id
    @EqualsAndHashCode.Include
    private String name;
    private String displayName;
    private String baseEndpoint;
    private String description;
    private Set<String> topics;

    @Embedded
    private AdapterContainerEntity adapterContainer;

    @ToString.Exclude
    @OneToMany(mappedBy = "adapter")
    @AuditJoinTable
    private List<ModelEntity> models = new ArrayList<>();

    @Override
    public String getId() {
        return getName();
    }
}