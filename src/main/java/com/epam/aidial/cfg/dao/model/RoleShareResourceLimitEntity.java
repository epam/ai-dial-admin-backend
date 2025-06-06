package com.epam.aidial.cfg.dao.model;

import jakarta.persistence.Embedded;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.PostLoad;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.envers.Audited;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Audited
public class RoleShareResourceLimitEntity {

    @EqualsAndHashCode.Include
    @EmbeddedId
    private RoleShareResourceLimitId id = new RoleShareResourceLimitId();

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("deploymentName")
    private DeploymentEntity deployment;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("roleName")
    private RoleEntity role;

    @Embedded
    private ShareResourceLimitEntity limit;

    @PostLoad
    private void postLoad() {
        if (limit == null) {
            limit = new ShareResourceLimitEntity();
        }
    }
}
