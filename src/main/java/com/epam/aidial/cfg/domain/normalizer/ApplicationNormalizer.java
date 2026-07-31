package com.epam.aidial.cfg.domain.normalizer;

import com.epam.aidial.cfg.domain.model.Application;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ApplicationNormalizer {

    public void normalize(Application application) {
        normalizeDisplayVersion(application);
        setEndpointToNullIfBlank(application);
    }

    private void normalizeDisplayVersion(Application application) {
        if (StringUtils.isBlank(application.getDisplayVersion())) {
            application.setDisplayVersion(null);
        }
    }

    private void setEndpointToNullIfBlank(Application application) {
        String endpoint = application.getEndpoint();
        if (StringUtils.isBlank(endpoint)) {
            application.setEndpoint(null);
        }
    }
}
