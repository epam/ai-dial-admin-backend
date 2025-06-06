package com.epam.aidial.cfg.dao.model;

import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Transient;
import org.springframework.data.domain.Persistable;

/**
 *  @see <a href="https://docs.spring.io/spring-data/jpa/reference/jpa/entity-persistence.html">Persisting Entities</a>
 */
@MappedSuperclass
public abstract class AbstractEntity<ID> implements Persistable<ID> {

    @Transient
    private boolean isNew = true;

    @Override
    public boolean isNew() {
        return isNew;
    }

    @PrePersist
    @PostLoad
    void markNotNew() {
        this.isNew = false;
    }
}
