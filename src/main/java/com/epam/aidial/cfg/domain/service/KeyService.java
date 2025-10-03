package com.epam.aidial.cfg.domain.service;


import com.epam.aidial.cfg.dao.jpa.KeyJpaRepository;
import com.epam.aidial.cfg.dao.mapper.KeyEntityMapper;
import com.epam.aidial.cfg.dao.model.KeyEntity;
import com.epam.aidial.cfg.domain.model.Key;
import com.epam.aidial.cfg.domain.resolver.key.KeyGeneratedAtResolver;
import com.epam.aidial.cfg.domain.validator.KeyValidator;
import com.epam.aidial.cfg.exception.EntityAlreadyExistsException;
import com.epam.aidial.cfg.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service("coreKeyService")
@RequiredArgsConstructor
public class KeyService {

    private static final String NOT_FOUND_MESSAGE_TEMPLATE = "Key with name %s does not exist";

    private final KeyJpaRepository keyJpaRepository;
    private final KeyEntityMapper mapper;
    private final KeyValidator keyValidator;
    private final KeyGeneratedAtResolver keyGeneratedAtResolver;
    private final HistoryService historyService;

    @Transactional(readOnly = true)
    public Collection<Key> getAllKeys() {
        return StreamSupport.stream(keyJpaRepository.findAll().spliterator(), false)
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
                .map(domainModel -> mapper.toEntity(domainModel, keyGeneratedAt, new KeyEntity()))
                .ifPresent(keyJpaRepository::save);
    }

    @Transactional
    public void updateKey(String name, Key domain) {
        KeyEntity keyEntity = keyJpaRepository.findById(name)
                .orElseThrow(() -> new EntityNotFoundException(NOT_FOUND_MESSAGE_TEMPLATE.formatted(name)));

        keyValidator.validateUpdate(name, domain, keyEntity);
        assertNewKeyValue(keyEntity.getKey(), domain.getKey());

        long keyGeneratedAt = keyGeneratedAtResolver.resolveKeyGeneratedAtValueDuringUpdate(domain, keyEntity);

        Optional.of(domain)
                .map(domainModel -> mapper.toEntity(domainModel, keyGeneratedAt, keyEntity))
                .ifPresent(keyJpaRepository::save);
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
    public Collection<Key> getAllAtRevision(Integer revision) {
        return historyService.getEntitiesAtRevision(revision, KeyEntity.class)
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
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
        if (keyJpaRepository.existsByKey(keyValue)) {
            throw new EntityAlreadyExistsException("Key with value " + keyValue + " already exists");
        }
    }
}
