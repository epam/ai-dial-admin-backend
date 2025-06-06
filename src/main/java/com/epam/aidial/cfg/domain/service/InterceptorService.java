package com.epam.aidial.cfg.domain.service;

import com.epam.aidial.cfg.dao.jpa.InterceptorJpaRepository;
import com.epam.aidial.cfg.dao.mapper.InterceptorEntityMapper;
import com.epam.aidial.cfg.dao.model.AssistantEntity;
import com.epam.aidial.cfg.dao.model.InterceptorEntity;
import com.epam.aidial.cfg.domain.model.Interceptor;
import com.epam.aidial.cfg.domain.validator.InterceptorValidator;
import com.epam.aidial.cfg.exception.EntityAlreadyExistsException;
import com.epam.aidial.cfg.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service("coreInterceptorService")
@RequiredArgsConstructor
public class InterceptorService {

    private static final String NOT_FOUND_MESSAGE_TEMPLATE = "Interceptor with name %s does not exist";

    private final InterceptorJpaRepository interceptorJpaRepository;
    private final InterceptorEntityMapper mapper;
    private final InterceptorValidator interceptorValidator;
    private final HistoryService historyService;

    @Transactional(readOnly = true)
    public Collection<Interceptor> getAll() {
        return StreamSupport.stream(interceptorJpaRepository.findAll().spliterator(), false)
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Interceptor get(String interceptorName) {
        return Optional.ofNullable(interceptorName)
                .flatMap(interceptorJpaRepository::findById)
                .map(mapper::toDomain)
                .orElseThrow(() -> new EntityNotFoundException("unable to find interceptor " + interceptorName));
    }

    @Transactional
    public void create(Interceptor interceptor) {
        assertNotExists(interceptor.getName());
        Optional.of(interceptor)
                .map(domainModel -> mapper.toEntity(domainModel, new InterceptorEntity()))
                .map(interceptorJpaRepository::save);
    }

    @Transactional
    public void update(String interceptorName, Interceptor interceptor) {
        interceptorValidator.validateUpdate(interceptorName, interceptor);
        InterceptorEntity interceptorEntity = interceptorJpaRepository.findById(interceptorName)
                .orElseThrow(() -> new EntityNotFoundException(NOT_FOUND_MESSAGE_TEMPLATE.formatted(interceptorName)));
        Optional.of(interceptor)
                .map(domainModel -> mapper.toEntity(domainModel, interceptorEntity))
                .ifPresent(interceptorJpaRepository::save);
    }

    @Transactional
    public void delete(String interceptorName) {
        assertExists(interceptorName);
        interceptorJpaRepository.deleteById(interceptorName);
    }

    @Transactional(readOnly = true)
    public boolean exists(String interceptorName) {
        return interceptorJpaRepository.existsById(interceptorName);
    }

    @Transactional(readOnly = true)
    public Interceptor getSnapshot(String interceptorName, Integer revision) {
        var entity = historyService.entitySnapshotAtRevision(revision, interceptorName, InterceptorEntity.class);
        return mapper.toDomain(entity);
    }

    private void assertExists(String name) {
        boolean exists = interceptorJpaRepository.existsById(name);
        if (!exists) {
            throw new EntityNotFoundException(NOT_FOUND_MESSAGE_TEMPLATE.formatted(name));
        }
    }

    private void assertNotExists(String name) {
        if (interceptorJpaRepository.existsById(name)) {
            throw new EntityAlreadyExistsException("Interceptor with name " + name + " already exists");
        }
    }
}
