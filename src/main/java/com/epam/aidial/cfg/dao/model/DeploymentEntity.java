package com.epam.aidial.cfg.dao.model;

import com.epam.aidial.cfg.dao.listener.DeploymentEntityListener;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.Any;
import org.hibernate.annotations.AnyDiscriminatorValue;
import org.hibernate.annotations.AnyKeyJavaClass;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;

import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@NoArgsConstructor
@Entity
@Audited
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "deployment_type", discriminatorType = DiscriminatorType.STRING)
@DiscriminatorValue("DEPLOYMENT")
@EntityListeners(DeploymentEntityListener.class)
public class DeploymentEntity extends AbstractEntity<String> {

    @Id
    @EqualsAndHashCode.Include
    private String name;
    @Enumerated(EnumType.STRING)
    private DeploymentTypeEntity type;

    @ToString.Exclude
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "deployment", orphanRemoval = true)
    private List<RoleLimitEntity> roleLimits = new ArrayList<>();
    private Boolean isPublic;
    @Embedded
    private LimitEntity defaultRoleLimit;

    @Any(fetch = FetchType.LAZY)
    @AnyKeyJavaClass(String.class)
    @Column(name = "type", insertable = false, updatable = false)
    @JoinColumn(name = "name", insertable = false, updatable = false)
    @AnyDiscriminatorValue(discriminator = "ADDON", entity = AddonEntity.class)
    @AnyDiscriminatorValue(discriminator = "APPLICATION", entity = ApplicationEntity.class)
    @AnyDiscriminatorValue(discriminator = "ASSISTANT", entity = AssistantEntity.class)
    @AnyDiscriminatorValue(discriminator = "MODEL", entity = ModelEntity.class)
    @AnyDiscriminatorValue(discriminator = "ROUTE", entity = RouteEntity.class)
    @AnyDiscriminatorValue(discriminator = "TOOL_SET", entity = ToolSetEntity.class)
    @NotAudited
    @ToString.Exclude
    private TimeTrackableEntity<String> owner;

    public DeploymentEntity(String name) {
        this.name = name;
    }

    @Override
    public String getId() {
        return name;
    }
}
