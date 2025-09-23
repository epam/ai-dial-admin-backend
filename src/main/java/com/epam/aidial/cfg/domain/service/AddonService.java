package com.epam.aidial.cfg.domain.service;

import com.epam.aidial.cfg.dao.jpa.AddonJpaRepository;
import com.epam.aidial.cfg.dao.mapper.AddonEntityMapper;
import com.epam.aidial.cfg.dao.model.AddonEntity;
import com.epam.aidial.cfg.domain.model.Addon;
import com.epam.aidial.cfg.domain.model.DomainObjectWithHash;
import com.epam.aidial.cfg.domain.validator.AddonValidator;
import com.epam.aidial.cfg.exception.EntityNotFoundException;
import com.epam.aidial.cfg.exception.OptimisticLockConflictException;
import com.epam.aidial.cfg.features.flag.annotation.FeatureFlagGate;
import com.epam.aidial.cfg.service.hashing.HashCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.epam.aidial.cfg.service.hashing.HashCalculator.ANY_HASH;

@Service("coreAddonService")
@RequiredArgsConstructor
@Slf4j
public class AddonService {

    private static final String NOT_FOUND_MESSAGE_TEMPLATE = "Addon with name %s does not exist";

    private final AddonJpaRepository addonJpaRepository;
    private final AddonEntityMapper mapper;
    private final DeploymentService deploymentService;
    private final AddonValidator addonValidator;
    private final HistoryService historyService;
    private final HashCalculator calculator;

    @Transactional(readOnly = true)
    public Collection<Addon> getAllAddons() {
        return StreamSupport.stream(addonJpaRepository.findAll().spliterator(), false)
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Addon getAddon(String addonName) {
        return tryGetAddon(addonName)
                .orElseThrow(() -> new EntityNotFoundException(NOT_FOUND_MESSAGE_TEMPLATE.formatted(addonName)));
    }

    @Transactional(readOnly = true)
    public DomainObjectWithHash<Addon> getAddonWithHash(String addonName) {
        var addon = getAddon(addonName);
        return new DomainObjectWithHash<>(addon, calculator.calculateHash(addon));
    }

    @Transactional(readOnly = true)
    public Optional<Addon> tryGetAddon(String addonName) {
        return Optional.ofNullable(addonName)
                .flatMap(addonJpaRepository::findById)
                .map(mapper::toDomain);
    }

    @FeatureFlagGate(featureFlag = "addonsSupported")
    @Transactional
    public void createAddon(Addon addon) {
        addonValidator.validateAddonCreation(addon);
        deploymentService.assertDeploymentNotExists(addon.getDeployment().getName());
        Optional.of(addon)
                .map(domainAddon -> mapper.toEntity(domainAddon, new AddonEntity()))
                .map(addonJpaRepository::save)
                .orElseThrow(() -> new RuntimeException("Unable to create addon " + addon.getDeployment().getName()));
    }

    @FeatureFlagGate(featureFlag = "addonsSupported")
    @Transactional
    public void updateAddon(String addonName, Addon addon) {
        performUpdate(addonName, addon, ANY_HASH);
    }

    @FeatureFlagGate(featureFlag = "addonsSupported")
    @Transactional
    public String updateAddon(String addonName, Addon addon, String hash) {
        if (hash == null) {
            throw new IllegalArgumentException(
                    "Hash must not be null. Use \"*\" to skip optimistic check.");
        }
        var savedModel = performUpdate(addonName, addon, hash);
        return calculator.calculateHash(mapper.toDomain(savedModel));
    }

    private AddonEntity performUpdate(String addonName, Addon addon, String hash) {
        addonValidator.validateUpdate(addonName, addon);
        var addonEntity = addonJpaRepository.findById(addonName)
                .orElseThrow(() -> new EntityNotFoundException(NOT_FOUND_MESSAGE_TEMPLATE.formatted(addonName)));
        assertNotConcurrencyOverwrite(addonEntity, hash);
        return addonJpaRepository.save(mapper.toEntity(addon, addonEntity));
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

    private void assertNotConcurrencyOverwrite(AddonEntity entity, String expectedHash) {
        if (ANY_HASH.equals(expectedHash)) {
            return;
        }
        var currentHash = calculator.calculateHash(mapper.toDomain(entity));
        if (!expectedHash.equals(currentHash)) {
            log.debug("Optimistic lock conflict on update: addonName={}, expectedHash={}, currentHash={}",
                    entity.getDeploymentName(), expectedHash, currentHash);
            throw new OptimisticLockConflictException("Optimistic lock conflict on update: addonName:'"
                    + entity.getDeploymentName() + "'. Reload the data.");
        }
    }

    private void assertExists(String addonName) {
        boolean exists = addonJpaRepository.existsById(addonName);
        if (!exists) {
            throw new EntityNotFoundException(NOT_FOUND_MESSAGE_TEMPLATE.formatted(addonName));
        }
    }
}
