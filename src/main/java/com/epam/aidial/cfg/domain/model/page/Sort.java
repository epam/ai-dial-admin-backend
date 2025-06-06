package com.epam.aidial.cfg.domain.model.page;

import lombok.Data;

@Data
public class Sort {
    private String column;
    private SortDirection direction;
}


