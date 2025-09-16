package com.epam.aidial.cfg.model;

public record DomainModelWithEtag<T>(T model, String etag) {
}
