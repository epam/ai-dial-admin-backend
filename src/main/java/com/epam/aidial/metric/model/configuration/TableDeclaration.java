package com.epam.aidial.metric.model.configuration;

public interface TableDeclaration {

    String getName();

    TableSource getSource();

    TableSchema getSchema();

}
