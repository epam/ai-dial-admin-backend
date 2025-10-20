package com.epam.aidial.cfg.service.transfer.importer.compatibility.backward.transformer;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.domain.model.route.Route;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@LogExecution
public class RouteToLatestVersionTransformer {

    public void transform(Map<String, Route> routes) {
        MapUtils.emptyIfNull(routes).values()
                .forEach(this::transform);
    }

    private void transform(Route route) {
        if (StringUtils.isBlank(route.getDisplayName())) {
            route.setDisplayName(route.getDeployment().getName());
        }
    }
}
