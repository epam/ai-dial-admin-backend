package com.epam.aidial.cfg.domain.normalizer;

import com.epam.aidial.cfg.domain.model.ToolSet;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class ToolSetNormalizer {

    public void normalize(ToolSet toolSet) {
        setEndpointToNullIfBlank(toolSet);
    }

    private void setEndpointToNullIfBlank(ToolSet toolSet) {
        String endpoint = toolSet.getEndpoint();
        if (StringUtils.isBlank(endpoint)) {
            toolSet.setEndpoint(null);
        }
    }
}
