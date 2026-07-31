package com.epam.aidial.cfg.domain.normalizer;

import com.epam.aidial.cfg.domain.model.Model;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ModelNormalizer {

    public void normalize(Model model) {
        normalizeDisplayVersion(model);
    }

    private void normalizeDisplayVersion(Model model) {
        if (StringUtils.isBlank(model.getDisplayVersion())) {
            model.setDisplayVersion(null);
        }
    }
}
