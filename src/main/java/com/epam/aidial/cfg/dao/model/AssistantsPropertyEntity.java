package com.epam.aidial.cfg.dao.model;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.envers.Audited;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Entity
@Audited
public class AssistantsPropertyEntity extends AbstractEntity<Long> {

    @Id
    @EqualsAndHashCode.Include
    private Long id;
    private String endpoint;
    @Embedded
    private FeaturesEntity features;
}
