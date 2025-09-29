package com.epam.aidial.cfg.domain.service;

import com.epam.aidial.cfg.dao.jpa.AddonJpaRepository;
import com.epam.aidial.cfg.dao.mapper.AddonEntityMapper;
import com.epam.aidial.cfg.dao.model.AddonEntity;
import com.epam.aidial.cfg.dao.model.RoleEntity;
import com.epam.aidial.cfg.domain.model.Addon;
import com.epam.aidial.cfg.domain.model.Deployment;
import com.epam.aidial.cfg.domain.model.RoleBased;
import com.epam.aidial.cfg.domain.model.RoleLimit;
import com.epam.aidial.cfg.domain.model.RoleShareResourceLimit;
import com.epam.aidial.cfg.domain.validator.AddonValidator;
import com.epam.aidial.cfg.exception.EntityNotFoundException;
import com.epam.aidial.cfg.features.flag.annotation.FeatureFlagGate;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.ListUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
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
        return tryGetAddon(addonName)
                .orElseThrow(() -> new EntityNotFoundException(NOT_FOUND_MESSAGE_TEMPLATE.formatted(addonName)));
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
                .map(domainAddon -> toEntity(domainAddon, new AddonEntity()))
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
                .map(domainAddon -> toEntity(domainAddon, addonEntity))
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
    public Collection<Addon> getAllAtRevision(Number revision) {
        return historyService.getEntitiesAtRevision(revision, AddonEntity.class)
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Transactional
    public void rollbackAddons(Number revision) {
        Collection<Addon> addons = getAllAtRevision(revision);
        List<String> ids = addons.stream().map(RoleBased::getDeployment).map(Deployment::getName).toList();
        addonJpaRepository.deleteAllExcept(ids);

        for (Addon addon : addons) {
            AddonEntity entity = addonJpaRepository.findById(addon.getDeployment().getName()).orElseGet(AddonEntity::new);
            AddonEntity addonEntity = toEntity(addon, entity);
            addonJpaRepository.save(addonEntity);
        }
    }

    private void assertExists(String addonName) {
        boolean exists = addonJpaRepository.existsById(addonName);
        if (!exists) {
            throw new EntityNotFoundException(NOT_FOUND_MESSAGE_TEMPLATE.formatted(addonName));
        }
    }

    private AddonEntity toEntity(Addon domain, AddonEntity entity) {
        List<RoleLimit> roleLimits = ListUtils.emptyIfNull(domain.getDeployment().getRoleLimits());
        List<RoleEntity> rolesForLimits = deploymentService.findRolesByNames(roleLimits.stream().map(RoleLimit::getRole).toList());

        List<RoleShareResourceLimit> roleShareResourceLimits = ListUtils.emptyIfNull(domain.getDeployment().getRoleShareResourceLimits());
        List<RoleEntity> rolesForResourceShareLimits = deploymentService.findRolesByNames(roleShareResourceLimits.stream().map(RoleShareResourceLimit::getRole).toList());

        return mapper.toEntity(domain, entity, roleLimits, rolesForLimits, roleShareResourceLimits, rolesForResourceShareLimits);
    }
}
