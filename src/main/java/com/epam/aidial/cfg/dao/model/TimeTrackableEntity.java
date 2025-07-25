package com.epam.aidial.cfg.dao.model;

import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;

import java.time.Instant;

@Getter
@Setter
@Audited
@MappedSuperclass
public abstract class TimeTrackableEntity extends AbstractEntity<String> {

    private Long createdAt;
    private Long updatedAt;

    @PrePersist
    void onCreate() {
        final long now = Instant.now().toEpochMilli();
        if (this.createdAt == null) {
            this.createdAt = now;
        }
        this.updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = Instant.now().toEpochMilli();
    }
}
