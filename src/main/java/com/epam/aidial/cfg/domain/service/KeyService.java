package com.epam.aidial.cfg.domain.service;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.dao.jpa.KeyJpaRepository;
import com.epam.aidial.cfg.dao.jpa.RoleJpaRepository;
import com.epam.aidial.cfg.dao.mapper.KeyEntityMapper;
import com.epam.aidial.cfg.dao.model.KeyEntity;
import com.epam.aidial.cfg.dao.model.RoleEntity;
import com.epam.aidial.cfg.domain.model.DomainObjectWithHash;
import com.epam.aidial.cfg.domain.model.Key;
import com.epam.aidial.cfg.domain.resolver.key.KeyGeneratedAtResolver;
import com.epam.aidial.cfg.domain.validator.KeyValidator;
import com.epam.aidial.cfg.exception.EntityAlreadyExistsException;
import com.epam.aidial.cfg.exception.EntityNotFoundException;
import com.google.api.client.util.Lists;
import com.epam.aidial.cfg.exception.OptimisticLockConflictException;
import com.epam.aidial.cfg.service.hashing.HashCalculator;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.SetUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.epam.aidial.cfg.service.hashing.HashCalculator.ANY_HASH;

@LogExecution
@Service
@RequiredArgsConstructor
@Slf4j
public class KeyService {

    private static final String NOT_FOUND_MESSAGE_TEMPLATE = "Key with name %s does not exist";

    private final KeyJpaRepository keyJpaRepository;
    private final RoleJpaRepository roleJpaRepository;
    private final KeyEntityMapper mapper;
    private final KeyValidator keyValidator;
    private final KeyGeneratedAtResolver keyGeneratedAtResolver;
    private final HistoryService historyService;
    private final HashCalculator calculator;

    @Transactional(readOnly = true)
    public Collection<Key> getAllKeys() {
        return StreamSupport.stream(keyJpaRepository.findAll().spliterator(), false)
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Collection<Key> getAllValidKeys() {
        return keyJpaRepository.findAllByValidityStateIsValidTrue().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Collection<Key> getAllByNames(List<String> names) {
        return StreamSupport.stream(keyJpaRepository.findAllById(names).spliterator(), false)
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Key getKey(String keyName) {
        return tryGetKey(keyName)
                .orElseThrow(() -> new EntityNotFoundException(NOT_FOUND_MESSAGE_TEMPLATE.formatted(keyName)));
    }

    @Transactional(readOnly = true)
    public Optional<Key> tryGetKey(String keyName) {
        return keyJpaRepository.findById(keyName)
                .map(mapper::toDomain);
    }

    @Transactional(readOnly = true)
    public DomainObjectWithHash<Key> getKeyWithHash(String keyName) {
        var key = getKey(keyName);
        return new DomainObjectWithHash<>(key, calculator.calculateHash(key));
    }

    @Transactional(readOnly = true)
    public Optional<Key> tryGetKeyByKeyValue(String keyValue) {
        return keyJpaRepository.findByKey(keyValue)
                .map(mapper::toDomain);
    }

    @Transactional
    public void createKey(Key key) {
        keyValidator.validateCreation(key);
        assertNotExists(key.getName());
        assertNotExistsByKeyValue(key.getKey());

        long keyGeneratedAt = keyGeneratedAtResolver.resolveKeyGeneratedAtValueDuringCreation();

        Optional.of(key)
                .map(domainModel -> toEntity(domainModel, keyGeneratedAt, new KeyEntity()))
                .ifPresent(keyJpaRepository::save);
    }

    @Transactional
    public void updateKey(String keyName, Key domain) {
        performUpdate(keyName, domain, ANY_HASH);
    }

    @Transactional
    public String updateKey(String keyName, Key domain, String hash) {
        if (hash == null) {
            throw new IllegalArgumentException(String.format(
                    "Hash must not be null. Use \"*\" to skip optimistic check. Key:%s.", keyName));
        }
        var savedKey = performUpdate(keyName, domain, hash);
        return calculator.calculateHash(mapper.toDomain(savedKey));
    }

    private KeyEntity performUpdate(String keyName, Key domain, String hash) {
        KeyEntity keyEntity = keyJpaRepository.findById(keyName)
                .orElseThrow(() -> new EntityNotFoundException(NOT_FOUND_MESSAGE_TEMPLATE.formatted(keyName)));
        keyValidator.validateUpdate(keyName, domain, keyEntity);
        assertNewKeyValue(keyEntity.getKey(), domain.getKey());
        assertNotConcurrencyOverwrite(keyEntity, hash);

        long keyGeneratedAt = keyGeneratedAtResolver.resolveKeyGeneratedAtValueDuringUpdate(domain, keyEntity);

        return keyJpaRepository.save(toEntity(domain, keyGeneratedAt, keyEntity));
    }

    private void assertNotConcurrencyOverwrite(KeyEntity entity, String expectedHash) {
        if (ANY_HASH.equals(expectedHash)) {
            return;
        }
        var currentHash = calculator.calculateHash(mapper.toDomain(entity));
        if (!expectedHash.equals(currentHash)) {
            log.debug("Optimistic lock conflict on update: keyName={}, expectedHash={}, currentHash={}",
                    entity.getName(), expectedHash, currentHash);
            throw new OptimisticLockConflictException(String.format("Optimistic lock conflict on update: keyName:'"
                    + "%s'. Reload the data.", entity.getName()));
        }
    }

    @Transactional
    public void deleteKey(String keyName) {
        assertExists(keyName);
        keyJpaRepository.deleteById(keyName);
    }

    @Transactional(readOnly = true)
    public boolean exists(String keyName) {
        return keyJpaRepository.existsById(keyName);
    }

    @Transactional(readOnly = true)
    public Key getSnapshot(String keyName, Integer revision) {
        var entity = historyService.entitySnapshotAtRevision(revision, keyName, KeyEntity.class);
        return mapper.toDomain(entity);
    }

    @Transactional(readOnly = true)
    public Collection<Key> getAllAtRevision(Number revision) {
        return historyService.getEntitiesAtRevision(revision, KeyEntity.class)
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Transactional
    public void rollbackKeys(Number revision) {
        Collection<Key> keys = getAllAtRevision(revision);
        keyJpaRepository.deleteAllExcept(keys.stream().map(Key::getName).collect(Collectors.toList()));

        for (Key key : keys) {
            KeyEntity entity = keyJpaRepository.findById(key.getName()).orElseGet(KeyEntity::new);
            KeyEntity keyEntity = toEntity(key, key.getKeyGeneratedAt(), entity);
            keyJpaRepository.save(keyEntity);
        }
    }

    private void assertExists(String keyName) {
        boolean exists = keyJpaRepository.existsById(keyName);
        if (!exists) {
            throw new EntityNotFoundException(NOT_FOUND_MESSAGE_TEMPLATE.formatted(keyName));
        }
    }

    private void assertNotExists(String keyName) {
        if (keyJpaRepository.existsById(keyName)) {
            throw new EntityAlreadyExistsException("Key with name " + keyName + " already exists");
        }
    }

    private void assertNewKeyValue(String keyValue, String newKeyValue) {
        if (!Objects.equals(keyValue, newKeyValue)) {
            assertNotExistsByKeyValue(newKeyValue);
        }
    }

    private void assertNotExistsByKeyValue(String keyValue) {
        if (keyValue != null && keyJpaRepository.existsByKey(keyValue)) {
            throw new EntityAlreadyExistsException("Key with value " + keyValue + " already exists");
        }
    }

    private KeyEntity toEntity(Key domain, long keyGeneratedAt, KeyEntity entity) {
        List<RoleEntity> roles = findRolesByNames(domain.getRoles());
        return mapper.toEntity(domain, keyGeneratedAt, entity, roles);
    }

    private List<RoleEntity> findRolesByNames(List<String> names) {
        if (CollectionUtils.isEmpty(names)) {
            return List.of();
        }

        List<RoleEntity> existingRoles = Lists.newArrayList(roleJpaRepository.findAllById(names));
        Set<String> existingRoleNames = existingRoles.stream().map(RoleEntity::getName).collect(Collectors.toSet());

        Set<String> namesDiff = SetUtils.difference(new HashSet<>(names), existingRoleNames);
        if (!namesDiff.isEmpty()) {
            throw new EntityNotFoundException("unable to find roles: " + namesDiff);
        }

        return existingRoles;
    }
}
