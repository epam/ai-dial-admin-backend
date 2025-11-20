package com.epam.aidial.cfg.service.config.transfer.importer.compatibility.backward.transformer;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.domain.model.Interceptor;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@LogExecution
public class InterceptorToLatestVersionTransformer {

    public void transform(Map<String, Interceptor> interceptors) {
        MapUtils.emptyIfNull(interceptors).values()
                .forEach(this::transform);
    }

    private void transform(Interceptor interceptor) {
        if (StringUtils.isBlank(interceptor.getDisplayName())) {
            interceptor.setDisplayName(interceptor.getName());
        }
    }
}
