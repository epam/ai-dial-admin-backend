package com.epam.aidial.ql;

import com.epam.aidial.expressions.FunctionsDatasource;
import com.epam.aidial.ql.model.Completable;
import com.epam.aidial.ql.model.Data;
import com.epam.aidial.ql.model.Table;

import java.util.Map;

public interface Engine {
    // TODO: move getTable method here
    // TODO: move getColumnAvailability method here
    String getName();
    FunctionsDatasource getFunctions();
    Map<String, Table> getTables();
    Data getData(Completable completable);
}
