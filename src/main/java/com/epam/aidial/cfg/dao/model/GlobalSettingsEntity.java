package com.epam.aidial.cfg.dao.model;

import com.epam.aidial.cfg.dao.converter.StringListJsonConverter;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.envers.Audited;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Entity
@Audited
public class GlobalSettingsEntity extends TimeTrackableEntity<Integer> {
    @Id
    @EqualsAndHashCode.Include
    private Integer id = 1;

    @Convert(converter = StringListJsonConverter.class)
    private List<String> globalInterceptors = new ArrayList<>();

    @NotNull
    @Override
    public Integer getId() {
        return id;
    }
}