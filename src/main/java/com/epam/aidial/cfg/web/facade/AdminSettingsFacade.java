package com.epam.aidial.cfg.web.facade;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.domain.model.AdminSettings;
import com.epam.aidial.cfg.domain.service.AdminSettingsService;
import com.epam.aidial.cfg.dto.AdminSettingsDto;
import com.epam.aidial.cfg.dto.DtoWithDomainHash;
import com.epam.aidial.cfg.web.facade.mapper.AdminSettingsDtoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@LogExecution
public class AdminSettingsFacade {

    private final AdminSettingsService adminSettingsService;
    private final AdminSettingsDtoMapper adminSettingsDtoMapper;

    public DtoWithDomainHash<AdminSettingsDto> getAdminSettingsWithHash() {
        var modelWithHash = adminSettingsService.getAdminSettingsWithHash();
        var dto = adminSettingsDtoMapper.toDto(modelWithHash.model());
        return new DtoWithDomainHash<>(dto, modelWithHash.hash());
    }

    public String updateCoreConfigVersion(String coreConfigVersion, String hash) {
        return adminSettingsService.updateCoreConfigVersion(coreConfigVersion, hash);
    }

    public AdminSettingsDto getAtRevision(Integer revision) {
        AdminSettings adminSettings = adminSettingsService.getAtRevision(revision);
        return adminSettingsDtoMapper.toDto(adminSettings);
    }
}
