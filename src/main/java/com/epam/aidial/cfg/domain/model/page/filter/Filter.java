package com.epam.aidial.cfg.domain.model.page.filter;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Filter {

    private String column;
    private FilterOperator operator;
    private String value;
}
