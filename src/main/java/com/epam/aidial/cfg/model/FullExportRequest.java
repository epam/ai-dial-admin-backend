package com.epam.aidial.cfg.model;

import com.epam.aidial.cfg.domain.model.ExportConfigComponentType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Set;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FullExportRequest extends ExportRequest {

    private Set<ExportConfigComponentType> componentTypes;
    private Set<String> topics;
}
