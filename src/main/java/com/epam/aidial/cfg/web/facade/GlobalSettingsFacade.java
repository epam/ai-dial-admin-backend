package com.epam.aidial.cfg.web.facade;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.domain.service.GlobalSettingsService;
import com.epam.aidial.cfg.dto.GlobalSettingsDto;
import com.epam.aidial.cfg.web.facade.mapper.GlobalSettingsDtoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@LogExecution
public class GlobalSettingsFacade {
    private final GlobalSettingsService globalSettingsService;
    private final GlobalSettingsDtoMapper mapper;

    public GlobalSettingsDto getGlobalSettings() {
        return mapper.toDto(globalSettingsService.getGlobalSettings());
    }

    public void saveGlobalSettings(GlobalSettingsDto globalSettingsDto) {
        globalSettingsService.saveGlobalSettings(mapper.toDomain(globalSettingsDto));
    }

    public GlobalSettingsDto getAtRevision(Integer revision) {
        return mapper.toDto(globalSettingsService.getAtRevision(revision));
    }
}