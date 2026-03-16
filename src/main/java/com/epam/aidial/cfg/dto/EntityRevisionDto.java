package com.epam.aidial.cfg.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EntityRevisionDto<T> {

    private T state;
    private Integer configRevisionId;
    private RevisionTypeDto revisionType;

    public enum RevisionTypeDto {
        ADD,
        MOD,
        DEL;
    }
}
