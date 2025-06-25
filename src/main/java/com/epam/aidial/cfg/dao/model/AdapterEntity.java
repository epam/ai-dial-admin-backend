package com.epam.aidial.cfg.dao.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.envers.Audited;

import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Entity
@Audited
public class AdapterEntity extends AbstractEntity<String> {

    @Id
    @EqualsAndHashCode.Include
    private String name;
    private String displayName;
    private String baseEndpoint;
    private String description;

    @ToString.Exclude
    @OneToMany(mappedBy = "adapter")
    private List<ModelEntity> models = new ArrayList<>();

    @Override
    public String getId() {
        return getName();
    }
}
