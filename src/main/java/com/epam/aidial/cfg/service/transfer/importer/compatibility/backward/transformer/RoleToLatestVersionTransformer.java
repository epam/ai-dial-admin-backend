package com.epam.aidial.cfg.service.transfer.importer.compatibility.backward.transformer;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.domain.model.Role;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@LogExecution
public class RoleToLatestVersionTransformer {

    public void transform(Map<String, Role> roles) {
        MapUtils.emptyIfNull(roles).values()
                .forEach(this::transform);
    }

    private void transform(Role role) {
        if (StringUtils.isBlank(role.getDisplayName())) {
            role.setDisplayName(role.getName());
        }
    }
}
