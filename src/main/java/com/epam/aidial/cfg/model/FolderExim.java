package com.epam.aidial.cfg.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FolderExim {
    private String id;
    private String name;
    private String folderId;
    private String type;
}
