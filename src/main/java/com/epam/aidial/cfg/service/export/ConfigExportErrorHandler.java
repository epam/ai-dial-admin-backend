package com.epam.aidial.cfg.service.export;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import lombok.Data;
import org.springframework.stereotype.Component;

@Component
@LogExecution
@Data
public class ConfigExportErrorHandler {

    private String lastErrorMessage = null;

}
