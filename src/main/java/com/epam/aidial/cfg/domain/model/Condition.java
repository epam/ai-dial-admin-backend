package com.epam.aidial.cfg.domain.model;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class Condition {

    @NotNull
    private String field;

    @NotNull
    private Operator operator;

    @NotNull
    private Object value;
}
