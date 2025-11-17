package com.epam.aidial.cfg.service.reload;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import lombok.Data;
import org.springframework.stereotype.Component;

@Component
@LogExecution
@Data
public class ConfigReloadErrorHandler {

    private String lastErrorMessage = null;

}
