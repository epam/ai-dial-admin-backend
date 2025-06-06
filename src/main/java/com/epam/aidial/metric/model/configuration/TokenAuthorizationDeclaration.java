package com.epam.aidial.metric.model.configuration;

import lombok.Data;

@Data
public class TokenAuthorizationDeclaration implements AuthorizationDeclaration {
    private String token;
}
