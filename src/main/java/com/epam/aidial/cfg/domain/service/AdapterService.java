package com.epam.aidial.cfg.domain.service;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.dao.jpa.AdapterJpaRepository;
import com.epam.aidial.cfg.dao.jpa.ModelJpaRepository;
import com.epam.aidial.cfg.dao.mapper.AdapterEntityMapper;
import com.epam.aidial.cfg.dao.model.AdapterEntity;
import com.epam.aidial.cfg.dao.model.ModelEntity;
import com.epam.aidial.cfg.domain.model.Adapter;
import com.epam.aidial.cfg.domain.model.DomainObjectWithHash;
import com.epam.aidial.cfg.domain.utils.ModelEndpointUtils;
import com.epam.aidial.cfg.domain.validator.AdapterValidator;
import com.epam.aidial.cfg.exception.EntityAlreadyExistsException;
import com.epam.aidial.cfg.exception.EntityNotFoundException;
import com.epam.aidial.cfg.exception.OptimisticLockConflictException;
import com.epam.aidial.cfg.service.hashing.HashCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.epam.aidial.cfg.service.hashing.HashCalculator.ANY_HASH;

@Service("coreAdapterService")
@RequiredArgsConstructor
@LogExecution
@Slf4j
public class AdapterService {

    private static final String NOT_FOUND_MESSAGE_TEMPLATE = "Adapter with name %s does not exist";

    private final ModelJpaRepository modelJpaRepository;
    private final AdapterJpaRepository adapterJpaRepository;
    private final AdapterEntityMapper mapper;
    private final AdapterValidator adapterValidator;
    private final HistoryService historyService;
    private final HashCalculator calculator;

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
    public DomainObjectWithHash<Adapter> getAdapterWithHash(String adapterName) {
        var adapter = get(adapterName);
        return new DomainObjectWithHash<>(adapter, calculator.calculateHash(adapter));
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
        performUpdate(adapterName, adapter, ANY_HASH);
    }

    @Transactional
    public String update(String adapterName, Adapter adapter, String hash) {
        if (hash == null) {
            throw new IllegalArgumentException(
                    "Hash must not be null. Use \"*\" to skip optimistic check.");
        }
        var savedModel = performUpdate(adapterName, adapter, hash);
        return calculator.calculateHash(mapper.toDomain(savedModel));
    }

    private AdapterEntity performUpdate(String adapterName, Adapter adapter, String hash) {
        adapterValidator.validateUpdate(adapterName, adapter);
        AdapterEntity adapterEntity = findByAdapterName(adapterName);
        assertNotConcurrencyOverwrite(adapterEntity, hash);
        return adapterJpaRepository.save(mapper.toEntity(adapter, adapterEntity));
    }

    @Transactional
    public void delete(String adapterName, boolean removeModel) {
        AdapterEntity adapterEntity = findByAdapterName(adapterName);
        List<ModelEntity> models = adapterEntity.getModels();

        if (CollectionUtils.isNotEmpty(models)) {
            if (removeModel) {
                modelJpaRepository.deleteAll(models);
            } else {
                String baseEndpoint = adapterEntity.getBaseEndpoint();
                models.forEach(model -> {
                    model.setAdapter(null);
                    model.setEndpoint(ModelEndpointUtils.concatEndpointAndPath(baseEndpoint, model.getAdapterCompletionEndpointPath()));
                });
            }
        }

        adapterJpaRepository.delete(adapterEntity);
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

    private AdapterEntity findByAdapterName(String adapterName) {
        return adapterJpaRepository.findById(adapterName)
                .orElseThrow(() -> new EntityNotFoundException(NOT_FOUND_MESSAGE_TEMPLATE.formatted(adapterName)));
    }

    private void assertNotConcurrencyOverwrite(AdapterEntity entity, String expectedHash) {
        if (ANY_HASH.equals(expectedHash)) {
            return;
        }
        var currentHash = calculator.calculateHash(mapper.toDomain(entity));
        if (!expectedHash.equals(currentHash)) {
            log.debug("Optimistic lock conflict on update: adapterName={}, expectedHash={}, currentHash={}",
                    entity.getName(), expectedHash, currentHash);
            throw new OptimisticLockConflictException("Optimistic lock conflict on update: adapterName:'"
                    + entity.getName() + "'. Reload the data.");
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
