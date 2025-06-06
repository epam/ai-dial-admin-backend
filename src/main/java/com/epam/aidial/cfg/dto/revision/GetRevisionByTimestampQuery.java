package com.epam.aidial.cfg.dto.revision;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class GetRevisionByTimestampQuery extends BaseGetRevisionQuery {
    @NotNull
    @Positive
    private Long timestamp;
}
