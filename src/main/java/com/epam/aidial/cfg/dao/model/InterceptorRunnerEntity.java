package com.epam.aidial.cfg.dao.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.envers.AuditJoinTable;
import org.hibernate.envers.Audited;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Entity
@Audited
public class InterceptorRunnerEntity extends TimeTrackableEntity<String> {

    @Id
    @EqualsAndHashCode.Include
    private String name;
    private String displayName;
    private String description;
    private String completionEndpoint;
    private String configurationEndpoint;

    @ToString.Exclude
    @OneToMany(mappedBy = "interceptorRunner")
    @AuditJoinTable
    private List<InterceptorEntity> interceptors = new ArrayList<>();

    @NotNull
    @Override
    public String getId() {
        return name;
    }
}