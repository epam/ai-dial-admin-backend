package com.epam.aidial.cfg.domain.service;


import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.dao.jpa.AdapterJpaRepository;
import com.epam.aidial.cfg.dao.mapper.AdapterEntityMapper;
import com.epam.aidial.cfg.dao.model.AdapterEntity;
import com.epam.aidial.cfg.domain.model.Adapter;
import com.epam.aidial.cfg.domain.validator.AdapterValidator;
import com.epam.aidial.cfg.exception.EntityAlreadyExistsException;
import com.epam.aidial.cfg.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.IterableUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service("coreAdapterService")
@RequiredArgsConstructor
@LogExecution
@Slf4j
public class AdapterService {

    private static final String NOT_FOUND_MESSAGE_TEMPLATE = "Adapter with name %s does not exist";
    private final AdapterJpaRepository adapterJpaRepository;
    private final AdapterEntityMapper mapper;
    private final AdapterValidator adapterValidator;
    private final HistoryService historyService;

    @Transactional(readOnly = true)
    public Collection<Adapter> getAll() {
        return StreamSupport.stream(adapterJpaRepository.findAll().spliterator(), false)
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Adapter get(String adapterName) {
        return Optional.ofNullable(adapterName)
                .flatMap(adapterJpaRepository::findById)
                .map(mapper::toDomain)
                .orElseThrow(() -> new EntityNotFoundException(NOT_FOUND_MESSAGE_TEMPLATE.formatted(adapterName)));
    }

    @Transactional(readOnly = true)
    public Adapter getByEndpoint(String adapterEndpoint) {
        return Optional.ofNullable(adapterEndpoint)
                .map(adapterJpaRepository::findByBaseEndpointOrderByNameAsc)
                .map(this::resolveAdapterFromAdaptersWithSameBaseEndpoint)
                .map(mapper::toDomain)
                .orElseThrow(() -> new EntityNotFoundException("Adapter with endpoint %s does not exist".formatted(adapterEndpoint)));
    }

    @Transactional
    public void create(Adapter adapter) {
        adapterValidator.validateAdapterCreation(adapter);
        assertNotExists(adapter.getName());
        Optional.of(adapter)
                .map(domainModel -> mapper.toEntity(domainModel, new AdapterEntity()))
                .map(adapterJpaRepository::save)
                .orElseThrow(() -> new RuntimeException("Unable to create adapter " + adapter.getName()));
    }

    @Transactional
    public void update(String adapterName, Adapter adapter) {
        adapterValidator.validateUpdate(adapterName, adapter);
        AdapterEntity adapterEntity = adapterJpaRepository.findById(adapterName)
                .orElseThrow(() -> new EntityNotFoundException(NOT_FOUND_MESSAGE_TEMPLATE.formatted(adapterName)));
        Optional.of(adapter)
                .map(domainModel -> mapper.toEntity(domainModel, adapterEntity))
                .map(adapterJpaRepository::save)
                .orElseThrow(() -> new RuntimeException("Unable to update adapter " + adapter.getName()));
    }

    @Transactional
    public void delete(String adapterName) {
        assertExists(adapterName);
        adapterJpaRepository.deleteById(adapterName);
    }

    @Transactional(readOnly = true)
    public boolean exists(String adapterName) {
        return adapterJpaRepository.existsById(adapterName);
    }

    @Transactional(readOnly = true)
    public Adapter getSnapshot(String adapterName, Integer revision) {
        var entity = historyService.entitySnapshotAtRevision(revision, adapterName, AdapterEntity.class);
        return mapper.toDomain(entity);
    }

    @Transactional(readOnly = true)
    public Collection<Adapter> getAllAtRevision(Integer revision) {
        return historyService.getEntitiesAtRevision(revision, AdapterEntity.class)
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    private void assertExists(String name) {
        boolean exists = adapterJpaRepository.existsById(name);
        if (!exists) {
            throw new EntityNotFoundException(NOT_FOUND_MESSAGE_TEMPLATE.formatted(name));
        }
    }

    private void assertNotExists(String name) {
        if (adapterJpaRepository.existsById(name)) {
            throw new EntityAlreadyExistsException("Adapter with name " + name + " already exists");
        }
    }

    // Such resolution is temporary and will be removed once there is an option for user to resolve
    // conflict on UI
    private AdapterEntity resolveAdapterFromAdaptersWithSameBaseEndpoint(Iterable<AdapterEntity> adapters) {
        if (IterableUtils.isEmpty(adapters)) {
            return null;
        }

        if (IterableUtils.size(adapters) != 1) {
            log.warn("Found multiple adapters with same base endpoint: {}. Will use the first one", adapters);
        }

        return IterableUtils.first(adapters);
    }
}
