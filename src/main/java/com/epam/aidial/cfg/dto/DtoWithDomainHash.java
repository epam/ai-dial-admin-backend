package com.epam.aidial.cfg.dto;

public record DtoWithDomainHash<T>(T dto, String hash) {}