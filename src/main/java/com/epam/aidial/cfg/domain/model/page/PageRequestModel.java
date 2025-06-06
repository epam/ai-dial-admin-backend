package com.epam.aidial.cfg.domain.model.page;

import com.epam.aidial.cfg.domain.model.page.filter.Filter;
import lombok.Data;

import java.util.List;

@Data
public class PageRequestModel {
    private int pageNumber;
    private int pageSize;
    private List<Sort> sorts;
    private List<Filter> filters;
}
