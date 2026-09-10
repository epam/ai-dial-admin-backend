package com.epam.aidial.cfg.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public abstract class ResourceNodeInfo<T extends ResourceNodeInfo<T>> {

    private String path;
    private String name;
    private String folderId;
    private Long updatedAt;
    private String author;
    private NodeType nodeType;
    private List<T> items;
    private String nextToken;
}