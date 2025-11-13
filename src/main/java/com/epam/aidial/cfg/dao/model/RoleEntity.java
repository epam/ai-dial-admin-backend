package com.epam.aidial.cfg.dao.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PreRemove;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.envers.AuditJoinTable;
import org.hibernate.envers.Audited;

import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Entity
@Audited
public class RoleEntity extends TimeTrackableEntity<String> {

    @Id
    @EqualsAndHashCode.Include
    private String name;
    private String description;
    private String displayName;
    @ToString.Exclude
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "role", orphanRemoval = true)
    @AuditJoinTable
    private List<RoleLimitEntity> limits = new ArrayList<>();
    @ToString.Exclude
    @ManyToMany(mappedBy = "roles")
    @AuditJoinTable
    private List<KeyEntity> keys = new ArrayList<>();
    @Column(columnDefinition = "CLOB")
    private String share;
    @Embedded
    private CostLimitEntity costLimit;

    @PreRemove
    public void preRemove() {
        keys.forEach(key -> key.getRoles().remove(this));
    }

    @Override
    public String getId() {
        return name;
    }

}
