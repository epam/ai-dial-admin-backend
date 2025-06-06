package com.epam.aidial.cfg.model;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ImportResources {

    private String path;
    private boolean flatImport;
    private ImportConflictResolutionStrategy conflictResolutionStrategy;
    private List<Rule> rules;

}
