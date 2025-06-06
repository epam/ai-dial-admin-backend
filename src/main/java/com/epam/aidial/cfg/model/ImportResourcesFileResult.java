package com.epam.aidial.cfg.model;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportResourcesFileResult {

    private String error;
    private List<ImportResourcesResult> importResults;

    public boolean hasError() {
        return error != null;
    }

}
