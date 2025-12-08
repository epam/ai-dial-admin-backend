package com.epam.aidial.cfg.dao.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.envers.Audited;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Entity
@Audited
public class AdminSettingsEntity extends TimeTrackableEntity<Integer> {

    @Id
    @EqualsAndHashCode.Include
    private Integer id;
    private String coreConfigVersion;

    @Override
    public Integer getId() {
        return id;
    }
}
