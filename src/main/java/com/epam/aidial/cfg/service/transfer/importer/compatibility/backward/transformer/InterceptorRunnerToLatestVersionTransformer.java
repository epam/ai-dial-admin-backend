package com.epam.aidial.cfg.service.transfer.importer.compatibility.backward.transformer;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.domain.model.InterceptorRunner;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@LogExecution
public class InterceptorRunnerToLatestVersionTransformer {

    public void transform(Map<String, InterceptorRunner> interceptorRunners) {
        MapUtils.emptyIfNull(interceptorRunners).values()
                .forEach(this::transform);
    }

    private void transform(InterceptorRunner interceptorRunner) {
        if (StringUtils.isBlank(interceptorRunner.getDisplayName())) {
            interceptorRunner.setDisplayName(interceptorRunner.getName());
        }
    }
}
