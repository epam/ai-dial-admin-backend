package com.epam.aidial.cfg.domain.normalizer;

import com.epam.aidial.cfg.domain.model.Model;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class ModelNormalizer {

    public void normalize(Model model) {
        setDisplayNameToNullIfBlank(model);
        setDisplayVersionToNullIfBlank(model);
    }

    private void setDisplayNameToNullIfBlank(Model model) {
        String displayName = model.getDisplayName();
        if (StringUtils.isBlank(displayName)) {
            model.setDisplayName(null);
        }
    }

    private void setDisplayVersionToNullIfBlank(Model model) {
        String displayVersion = model.getDisplayVersion();
        if (StringUtils.isBlank(displayVersion)) {
            model.setDisplayVersion(null);
        }
    }
}
