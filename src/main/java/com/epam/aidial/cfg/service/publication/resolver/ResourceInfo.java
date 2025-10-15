package com.epam.aidial.cfg.service.publication.resolver;

import com.epam.aidial.cfg.client.dto.PublicationResourceDto;
import com.epam.aidial.cfg.client.dto.PublicationStatusDto;

public record ResourceInfo(PublicationResourceDto resource, String resourceUrl, PublicationStatusDto status) {
}
