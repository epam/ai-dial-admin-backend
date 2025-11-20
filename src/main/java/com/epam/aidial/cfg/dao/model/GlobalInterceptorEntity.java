package com.epam.aidial.cfg.dao.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.envers.Audited;
import org.jetbrains.annotations.NotNull;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Entity
@Audited
public class GlobalInterceptorEntity extends TimeTrackableEntity<String> {
    @Id
    @EqualsAndHashCode.Include
    private String name;

    private Integer interceptorOrder;

    @NotNull
    @Override
    public String getId() {
        return name;
    }
}