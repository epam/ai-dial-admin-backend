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
public class FolderInfo {

    private String name;
    private String parentPath;
    private String bucket;
    private String path;
    private List<FolderInfo> items;
}
