package com.epam.aidial.cfg.domain.normalizer;

import com.epam.aidial.cfg.domain.model.SecuredResource;
import com.epam.aidial.cfg.domain.model.ToolSet;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ToolSetNormalizer {

    private final ResourceAuthSettingsNormalizer resourceAuthSettingsNormalizer;

    public void normalize(ToolSet toolSet) {
        setEndpointToNullIfBlank(toolSet);

        SecuredResource deployment = toolSet.getDeployment();
        if (deployment != null) {
            resourceAuthSettingsNormalizer.normalize(deployment.getAuthSettings());
        }
    }

    private void setEndpointToNullIfBlank(ToolSet toolSet) {
        String endpoint = toolSet.getEndpoint();
        if (StringUtils.isBlank(endpoint)) {
            toolSet.setEndpoint(null);
        }
    }
}
