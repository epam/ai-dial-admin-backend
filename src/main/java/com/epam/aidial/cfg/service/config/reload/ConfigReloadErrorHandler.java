package com.epam.aidial.cfg.service.config.reload;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.service.config.ConfigSyncErrorHandler;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;

@Component
@LogExecution
@Data
public class ConfigReloadErrorHandler implements ConfigSyncErrorHandler {

    private String lastErrorMessage = null;

    @Override
    @Nullable
    public String getPrefixedLastErrorMessage() {
        return StringUtils.isNotBlank(lastErrorMessage)
                ? "Reload error: " + lastErrorMessage
                : null;
    }

}
