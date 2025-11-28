package com.epam.aidial.cfg.dao.model;

import com.epam.aidial.cfg.dao.listener.validitystate.ValidityStateAwareEntityListener;
import jakarta.persistence.Embedded;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;

@Getter
@Setter
@Audited
@MappedSuperclass
@EntityListeners(ValidityStateAwareEntityListener.class)
public abstract class ValidityStateAwareEntity<ID> extends TimeTrackableEntity<ID> {

    @Embedded
    private ValidityStateEntity validityState;
}
