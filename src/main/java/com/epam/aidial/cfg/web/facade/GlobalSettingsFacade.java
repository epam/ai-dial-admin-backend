package com.epam.aidial.cfg.web.facade;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.domain.service.GlobalSettingsService;
import com.epam.aidial.cfg.dto.DtoWithDomainHash;
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

    public DtoWithDomainHash<GlobalSettingsDto> getGlobalSettingsWithHash() {
        var globalSettingsWithHash = globalSettingsService.getGlobalSettingsWithHash();
        var dto = mapper.toDto(globalSettingsWithHash.model());
        return new DtoWithDomainHash<>(dto, globalSettingsWithHash.hash());
    }

    public void updateGlobalSettings(GlobalSettingsDto globalSettingsDto) {
        globalSettingsService.update(mapper.toDomain(globalSettingsDto));
    }

    public String updateGlobalSettings(GlobalSettingsDto globalSettingsDto, String hash) {
        var value = mapper.toDomain(globalSettingsDto);
        return globalSettingsService.update(value, hash);
    }

    public GlobalSettingsDto getAtRevision(Integer revision) {
        return mapper.toDto(globalSettingsService.getAtRevision(revision));
    }
}