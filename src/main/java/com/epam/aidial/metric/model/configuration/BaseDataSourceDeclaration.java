package com.epam.aidial.metric.model.configuration;

import lombok.Data;

@Data
public abstract class BaseDataSourceDeclaration {
    private String url;
    private AuthorizationDeclaration auth;
}
