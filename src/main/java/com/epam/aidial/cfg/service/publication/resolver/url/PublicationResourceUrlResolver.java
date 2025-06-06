package com.epam.aidial.cfg.service.publication.resolver.url;

import com.epam.aidial.cfg.client.dto.PublicationResourceDto;
import com.epam.aidial.cfg.client.dto.PublicationStatusDto;
import com.epam.aidial.cfg.configuration.logging.LogExecution;
import org.springframework.stereotype.Component;

@Component
@LogExecution
public class PublicationResourceUrlResolver {

    public String resolveUrl(PublicationResourceDto publicationResource, PublicationStatusDto status) {
        return switch (publicationResource.getAction()) {
            case ADD -> resolveUrlForPublishing(publicationResource, status);
            case DELETE -> resolveUrlForUnPublishing(publicationResource);
        };
    }

    private String resolveUrlForPublishing(PublicationResourceDto publicationResource, PublicationStatusDto status) {
        return switch (status) {
            case PENDING -> publicationResource.getReviewUrl();
            case APPROVED -> publicationResource.getTargetUrl();
            case REJECTED -> publicationResource.getSourceUrl();
        };
    }

    private String resolveUrlForUnPublishing(PublicationResourceDto publicationResource) {
        return publicationResource.getTargetUrl();
    }
}
