package com.epam.aidial.cfg.domain.model;

public record ModelWithHash<T>(T model, String hash) {}
