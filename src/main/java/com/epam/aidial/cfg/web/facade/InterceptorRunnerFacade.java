package com.epam.aidial.cfg.web.facade;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.domain.model.InterceptorRunner;
import com.epam.aidial.cfg.domain.service.InterceptorRunnerService;
import com.epam.aidial.cfg.dto.DtoWithDomainHash;
import com.epam.aidial.cfg.dto.InterceptorRunnerDto;
import com.epam.aidial.cfg.web.facade.mapper.InterceptorRunnerDtoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@LogExecution
public class InterceptorRunnerFacade {

    private final InterceptorRunnerService interceptorRunnerService;
    private final InterceptorRunnerDtoMapper mapper;

    public Collection<InterceptorRunnerDto> getAllInterceptorRunners() {
        return interceptorRunnerService.getAll()
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    public InterceptorRunnerDto getInterceptorRunner(String interceptorRunnerName) {
        InterceptorRunner interceptorRunner = interceptorRunnerService.get(interceptorRunnerName);
        return mapper.toDto(interceptorRunner);
    }

    public DtoWithDomainHash<InterceptorRunnerDto> getInterceptorRunnerWithHash(String id) {
        var interceptorRunnerWithHash = interceptorRunnerService.getInterceptorRunnerWithHash(id);
        return new DtoWithDomainHash<>(mapper.toDto(interceptorRunnerWithHash.model()), interceptorRunnerWithHash.hash());
    }

    public void createInterceptorRunner(InterceptorRunnerDto interceptorRunnerDto) {
        Optional.of(interceptorRunnerDto)
                .map(mapper::toDomain)
                .ifPresent(interceptorRunnerService::create);
    }

    public String updateInterceptorRunner(String interceptorRunnerName, InterceptorRunnerDto interceptorRunnerDto, String hash) {
        InterceptorRunner value = mapper.toDomain(interceptorRunnerDto);
        return interceptorRunnerService.update(interceptorRunnerName, value, hash);
    }

    public void deleteInterceptorRunner(String interceptorRunnerName, boolean removeInterceptor) {
        interceptorRunnerService.delete(interceptorRunnerName, removeInterceptor);
    }

    public InterceptorRunnerDto getSnapshot(String interceptorRunnerName, Integer revision) {
        InterceptorRunner interceptorRunner = interceptorRunnerService.getSnapshot(interceptorRunnerName, revision);
        return mapper.toDto(interceptorRunner);
    }

    public Collection<InterceptorRunnerDto> getAllAtRevision(Integer revision) {
        return interceptorRunnerService.getAllAtRevision(revision)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }
}