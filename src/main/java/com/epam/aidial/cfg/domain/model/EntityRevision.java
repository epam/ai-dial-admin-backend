package com.epam.aidial.cfg.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EntityRevision<T> {

    private T state;
    private Integer configRevisionId;
    private RevisionType revisionType;

    public enum RevisionType {
        ADD,
        MOD,
        DEL;
    }
}
