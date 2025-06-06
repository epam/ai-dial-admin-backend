package com.epam.aidial.ql.model;

import java.util.List;

public interface Table extends From {
    String getName();

    List<Sort> getAdditionalSorts();
}
