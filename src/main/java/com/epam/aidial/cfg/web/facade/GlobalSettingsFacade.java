package com.epam.aidial.cfg.web.facade;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.domain.service.GlobalSettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

@RequiredArgsConstructor
@Service
@LogExecution
public class GlobalSettingsFacade {
    private final GlobalSettingsService globalSettingsService;

    public Collection<String> getAllGlobalInterceptors() {
        return globalSettingsService.getAllGlobalInterceptors();
    }

    public void saveGlobalInterceptors(List<String> globalInterceptorIds) {
        globalSettingsService.saveGlobalInterceptors(globalInterceptorIds);
    }

    public Collection<String> getAllAtRevision(Integer revision) {
        return globalSettingsService.getAllAtRevision(revision);
    }
}