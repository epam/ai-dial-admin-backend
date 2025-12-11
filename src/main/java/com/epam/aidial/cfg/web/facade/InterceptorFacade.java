package com.epam.aidial.cfg.web.facade;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.domain.model.Interceptor;
import com.epam.aidial.cfg.domain.service.InterceptorService;
import com.epam.aidial.cfg.dto.CoreWithDomainHash;
import com.epam.aidial.cfg.dto.DtoWithDomainHash;
import com.epam.aidial.cfg.dto.EntitySyncStateDto;
import com.epam.aidial.cfg.dto.InterceptorDto;
import com.epam.aidial.cfg.service.core.CoreInterceptorService;
import com.epam.aidial.cfg.web.facade.mapper.EntitySyncStateDtoMapper;
import com.epam.aidial.cfg.web.facade.mapper.InterceptorDtoMapper;
import com.epam.aidial.core.config.CoreInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@LogExecution
public class InterceptorFacade {

    private final InterceptorService interceptorService;
    private final InterceptorDtoMapper mapper;
    private final CoreInterceptorService coreInterceptorService;
    private final EntitySyncStateDtoMapper entitySyncStateDtoMapper;

    public Collection<InterceptorDto> getAllInterceptors() {
        return interceptorService.getAll()
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    public InterceptorDto getInterceptor(String interceptorName) {
        Interceptor interceptor = interceptorService.get(interceptorName);
        return mapper.toDto(interceptor);
    }

    public DtoWithDomainHash<InterceptorDto> getInterceptorWithHash(String interceptorName) {
        var interceptorWithHash = interceptorService.getInterceptorWithHash(interceptorName);
        return new DtoWithDomainHash<>(mapper.toDto(interceptorWithHash.model()), interceptorWithHash.hash());
    }

    public CoreWithDomainHash<CoreInterceptor> getCoreInterceptorWithHash(String interceptorName) {
        return coreInterceptorService.getCoreInterceptorWithHash(interceptorName);
    }

    public EntitySyncStateDto getSyncState(String interceptorName) {
        var syncState = coreInterceptorService.getSyncState(interceptorName);
        return entitySyncStateDtoMapper.toDto(syncState);
    }

    public void createInterceptor(InterceptorDto interceptorDto) {
        Optional.of(interceptorDto)
                .map(mapper::toDomain)
                .ifPresent(interceptorService::create);
    }

    public String updateInterceptor(String interceptorName, InterceptorDto interceptorDto, String hash) {
        Interceptor value = mapper.toDomain(interceptorDto);
        return interceptorService.update(interceptorName, value, hash);
    }

    public String updateInterceptor(String interceptorName, CoreInterceptor coreInterceptor, String hash) {
        return coreInterceptorService.updateInterceptor(interceptorName, coreInterceptor, hash);
    }

    public void deleteInterceptor(String interceptorName) {
        interceptorService.delete(interceptorName);
    }

    public InterceptorDto getSnapshot(String interceptorName, Integer revision) {
        Interceptor interceptor = interceptorService.getSnapshot(interceptorName, revision);
        return mapper.toDto(interceptor);
    }

    public Collection<InterceptorDto> getAllAtRevision(Integer revision) {
        return interceptorService.getAllAtRevision(revision)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    public void refreshEndpoints() {
        interceptorService.refreshEndpoints();
    }
}
