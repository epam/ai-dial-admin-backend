package com.epam.aidial.cfg.domain.service;

import com.epam.aidial.cfg.dao.jpa.AddonJpaRepository;
import com.epam.aidial.cfg.dao.mapper.AddonEntityMapper;
import com.epam.aidial.cfg.dao.model.AddonEntity;
import com.epam.aidial.cfg.dao.model.KeyEntity;
import com.epam.aidial.cfg.domain.model.Addon;
import com.epam.aidial.cfg.domain.model.Key;
import com.epam.aidial.cfg.domain.validator.AddonValidator;
import com.epam.aidial.cfg.exception.EntityNotFoundException;
import com.epam.aidial.cfg.features.flag.annotation.FeatureFlagGate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service("coreAddonService")
@RequiredArgsConstructor
public class AddonService {

    private static final String NOT_FOUND_MESSAGE_TEMPLATE = "Addon with name %s does not exist";

    private final AddonJpaRepository addonJpaRepository;
    private final AddonEntityMapper mapper;
    private final DeploymentService deploymentService;
    private final AddonValidator addonValidator;
    private final HistoryService historyService;

    @Transactional(readOnly = true)
    public Collection<Addon> getAllAddons() {
        return StreamSupport.stream(addonJpaRepository.findAll().spliterator(), false)
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Addon getAddon(String addonName) {
        return Optional.ofNullable(addonName)
                .flatMap(addonJpaRepository::findById)
                .map(mapper::toDomain)
                .orElseThrow(() -> new EntityNotFoundException(NOT_FOUND_MESSAGE_TEMPLATE.formatted(addonName)));
    }

    @FeatureFlagGate(featureFlag = "addonsSupported")
    @Transactional
    public void createAddon(Addon addon) {
        deploymentService.assertDeploymentNotExists(addon.getDeployment().getName());
        Optional.of(addon)
                .map(domainAddon -> mapper.toEntity(domainAddon, new AddonEntity()))
                .map(addonJpaRepository::save)
                .orElseThrow(() -> new RuntimeException("Unable to create addon " + addon.getDeployment().getName()));
    }

    @FeatureFlagGate(featureFlag = "addonsSupported")
    @Transactional
    public void updateAddon(String addonName, Addon addon) {
        addonValidator.validateUpdate(addonName, addon);
        AddonEntity addonEntity = addonJpaRepository.findById(addonName)
                .orElseThrow(() -> new EntityNotFoundException(NOT_FOUND_MESSAGE_TEMPLATE.formatted(addonName)));
        Optional.of(addon)
                .map(domainAddon -> mapper.toEntity(domainAddon, addonEntity))
                .map(addonJpaRepository::save)
                .orElseThrow(() -> new RuntimeException("Unable to update addon " + addon.getDeployment().getName()));
    }

    @FeatureFlagGate(featureFlag = "addonsSupported")
    @Transactional
    public void deleteAddon(String addonName) {
        assertExists(addonName);
        addonJpaRepository.deleteById(addonName);
    }

    @Transactional(readOnly = true)
    public boolean exists(String addonName) {
        return addonJpaRepository.existsById(addonName);
    }

    @Transactional(readOnly = true)
    public Addon getSnapshot(String addonName, Integer revision) {
        var entity = historyService.entitySnapshotAtRevision(revision, addonName, AddonEntity.class);
        return mapper.toDomain(entity);
    }

    @Transactional(readOnly = true)
    public Collection<Addon> getAllAtRevision(Integer revision) {
        return historyService.getEntitiesAtRevision(revision, AddonEntity.class)
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    private void assertExists(String addonName) {
        boolean exists = addonJpaRepository.existsById(addonName);
        if (!exists) {
            throw new EntityNotFoundException(NOT_FOUND_MESSAGE_TEMPLATE.formatted(addonName));
        }
    }
}
