package com.epam.aidial.cfg.dao.model;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@Setter
@Audited
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class TimeTrackableEntity<ID> extends AbstractEntity<ID> {
    @CreatedDate
    @Column(name = "created_at_ms")
    @NotAudited
    // Using Long instead of long since this field is not audited
    // and when reading from XXX_aud tables is deserialized as null instead of 0
    private Long createdAt;
    @LastModifiedDate
    @Column(name = "updated_at_ms")
    @NotAudited
    // Using Long instead of long since this field is not audited
    // and when reading from XXX_aud tables is deserialized as null instead of 0
    private Long updatedAt;
}
