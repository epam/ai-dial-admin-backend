package com.epam.aidial.cfg.dao.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.envers.Audited;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
@NoArgsConstructor
@Entity
@Audited
@DiscriminatorValue("SECURED_RESOURCE")
public class SecuredResourceEntity extends DeploymentEntity {

    @Embedded
    private ResourceAuthSettingsEntity authSettings;
}
