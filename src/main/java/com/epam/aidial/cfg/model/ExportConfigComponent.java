package com.epam.aidial.cfg.model;

import com.epam.aidial.cfg.domain.model.ExportConfigComponentType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;

import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExportConfigComponent {

    private String name;
    private ExportConfigComponentType type;
    private Set<ExportConfigComponentType> dependencies = new HashSet<>();

    public ExportConfigComponent(String name, ExportConfigComponentType type) {
        this.name = name;
        this.type = type;
    }

    public void addDependencies(Set<ExportConfigComponentType> dependencies) {
        if (CollectionUtils.isNotEmpty(dependencies)) {
            this.getDependencies().addAll(dependencies);
        }
    }

}
