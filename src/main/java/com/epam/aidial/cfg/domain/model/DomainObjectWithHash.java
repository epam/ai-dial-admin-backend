package com.epam.aidial.cfg.domain.model;

public record DomainObjectWithHash<T>(T model, String hash) {}
