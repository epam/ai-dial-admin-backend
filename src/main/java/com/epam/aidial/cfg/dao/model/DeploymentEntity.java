package com.epam.aidial.cfg.dao.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.envers.AuditJoinTable;
import org.hibernate.envers.Audited;

import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@NoArgsConstructor
@Entity
@Audited
public class DeploymentEntity extends AbstractEntity<String> {

    @Id
    @EqualsAndHashCode.Include
    private String name;
    @Enumerated(EnumType.STRING)
    private DeploymentTypeEntity type;

    @ToString.Exclude
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "deployment", orphanRemoval = true)
    @AuditJoinTable
    private List<RoleLimitEntity> roleLimits = new ArrayList<>();
    @ToString.Exclude
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "deployment", orphanRemoval = true)
    @AuditJoinTable
    private List<RoleShareResourceLimitEntity> roleShareResourceLimits = new ArrayList<>();
    private Boolean isPublic;
    @Embedded
    private LimitEntity defaultRoleLimit;
    @Embedded
    private ShareResourceLimitEntity defaultRoleShareResourceLimit;

    public DeploymentEntity(String name) {
        this.name = name;
    }

    @Override
    public String getId() {
        return name;
    }
}
