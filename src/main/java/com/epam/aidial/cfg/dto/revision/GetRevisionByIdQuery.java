package com.epam.aidial.cfg.dto.revision;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class GetRevisionByIdQuery extends BaseGetRevisionQuery {
    @NotNull
    private Integer id;
}
