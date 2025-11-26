package com.epam.aidial.cfg.domain.normalizer;

import com.epam.aidial.cfg.domain.model.Application;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class ApplicationNormalizer {

    public void normalize(Application application) {
        setEndpointToNullIfBlank(application);
    }

    private void setEndpointToNullIfBlank(Application application) {
        String endpoint = application.getEndpoint();
        if (StringUtils.isBlank(endpoint)) {
            application.setEndpoint(null);
        }
    }
}
