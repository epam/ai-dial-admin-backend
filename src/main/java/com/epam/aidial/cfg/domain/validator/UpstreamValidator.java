package com.epam.aidial.cfg.domain.validator;

import com.epam.aidial.cfg.domain.model.Upstream;
import com.epam.aidial.cfg.domain.model.UpstreamInterface;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Validates the {@code interfaces} map and {@code baseUrl} of each upstream on an entity's
 * {@code upstreams} list. Unlike DIAL Core, which silently sends no {@code X-UPSTREAM-ENDPOINT} for an
 * interface it cannot resolve, the admin backend rejects that at authoring time.
 */
@Component
public class UpstreamValidator {

    public void validate(List<Upstream> upstreams, Set<String> allowedTypes, String entityKind, String entityName) {
        if (CollectionUtils.isEmpty(upstreams)) {
            return;
        }

        for (int i = 0; i < upstreams.size(); i++) {
            validate(upstreams.get(i), i, allowedTypes, entityKind, entityName);
        }
    }

    private void validate(Upstream upstream, int index, Set<String> allowedTypes, String entityKind, String entityName) {
        Map<String, UpstreamInterface> interfaces = upstream.getInterfaces();
        String baseUrl = upstream.getBaseUrl();

        if (StringUtils.isNotBlank(baseUrl) && EndpointValidator.isInvalidUrl(baseUrl)) {
            throw new IllegalArgumentException(
                    "Invalid base URL '%s' for upstreams[%d]. %s: %s".formatted(baseUrl, index, entityKind, entityName));
        }

        if (MapUtils.isEmpty(interfaces)) {
            return;
        }

        if (StringUtils.isBlank(upstream.getId())) {
            throw new IllegalArgumentException(
                    "An upstream declaring interfaces requires an id. upstreams[%d]. %s: %s"
                            .formatted(index, entityKind, entityName));
        }

        interfaces.forEach((type, upstreamInterface) -> {
            if (!allowedTypes.contains(type)) {
                throw new IllegalArgumentException(
                        "Unsupported interface type '%s'. %s supports: %s. %s: %s"
                                .formatted(type, entityKind, String.join(", ", allowedTypes.stream().sorted().toList()),
                                        entityKind, entityName));
            }

            String endpoint = upstreamInterface == null ? null : upstreamInterface.getEndpoint();
            if (StringUtils.isBlank(endpoint)) {
                if (StringUtils.isBlank(baseUrl)) {
                    throw new IllegalArgumentException(
                            "Interface '%s' declares no endpoint and upstreams[%d] declares no baseUrl. %s: %s"
                                    .formatted(type, index, entityKind, entityName));
                }
                return;
            }
            if (EndpointValidator.isInvalidUrl(endpoint)) {
                throw new IllegalArgumentException(
                        "Invalid endpoint '%s' for interface '%s' on upstreams[%d]. %s: %s"
                                .formatted(endpoint, type, index, entityKind, entityName));
            }
        });
    }
}
