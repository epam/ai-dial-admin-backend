package com.epam.aidial.cfg.web.facade;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.domain.model.Addon;
import com.epam.aidial.cfg.domain.service.AddonService;
import com.epam.aidial.cfg.dto.AddonDto;
import com.epam.aidial.cfg.dto.DtoWithDomainHash;
import com.epam.aidial.cfg.web.facade.mapper.AddonDtoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@LogExecution
public class AddonFacade {

    private final AddonService addonService;
    private final AddonDtoMapper mapper;

    public Collection<AddonDto> getAllAddons() {
        return addonService.getAllAddons()
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    public AddonDto getAddon(String addonName) {
        Addon addon = addonService.getAddon(addonName);
        return mapper.toDto(addon);
    }

    public DtoWithDomainHash<AddonDto> getAddonWithHash(String addonName) {
        var addonWithHash = addonService.getAddonWithHash(addonName);
        return new DtoWithDomainHash<>(mapper.toDto(addonWithHash.model()), addonWithHash.hash());
    }

    public void createAddon(AddonDto addonDto) {
        Optional.of(addonDto)
                .map(mapper::toDomain)
                .ifPresent(addonService::createAddon);
    }

    public String updateAddon(String addonName, AddonDto addonDto, String hash) {
        Addon value = mapper.toDomain(addonDto);
        return addonService.updateAddon(addonName, value, hash);
    }

    public void deleteAddon(String addonName) {
        addonService.deleteAddon(addonName);
    }

    public AddonDto getSnapshot(String addonName, Integer revision) {
        Addon addon = addonService.getSnapshot(addonName, revision);
        return mapper.toDto(addon);
    }

    public Collection<AddonDto> getAllAtRevision(Integer revision) {
        return addonService.getAllAtRevision(revision)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }
}
