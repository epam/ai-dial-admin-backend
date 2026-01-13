package com.epam.aidial.cfg.dao.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.PreRemove;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.envers.Audited;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Entity
@Audited
public class KeyEntity extends ValidityStateAwareEntity<String> {

    @Id
    @EqualsAndHashCode.Include
    private String name;
    @Column(name = "key_value", unique = true)
    private String key;
    private String displayName;
    private Set<String> topics;
    private String project;
    private boolean secured;
    @ToString.Exclude
    @ManyToMany
    @JoinTable(
            name = "role_key",
            joinColumns = @JoinColumn(name = "key_name"),
            inverseJoinColumns = @JoinColumn(name = "role_name")
    )
    private List<RoleEntity> roles = new ArrayList<>();
    private String description;
    private String projectContactPoint;
    @Column(name = "expires_at_ms")
    private Long expiresAt;
    @Column(name = "key_value_generated_at_ms")
    private long keyGeneratedAt;

    @PreRemove
    public void preRemove() {
        roles.forEach(role -> role.getKeys().remove(this));
    }

    @Override
    public String getId() {
        return name;
    }
}