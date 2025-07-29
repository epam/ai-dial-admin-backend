package com.epam.aidial.cfg.web.facade;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.domain.model.Interceptor;
import com.epam.aidial.cfg.domain.service.InterceptorService;
import com.epam.aidial.cfg.dto.InterceptorDto;
import com.epam.aidial.cfg.web.facade.mapper.InterceptorDtoMapper;
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

    public void createInterceptor(InterceptorDto interceptorDto) {
        Optional.of(interceptorDto)
                .map(mapper::toDomain)
                .ifPresent(interceptorService::create);
    }

    public void updateInterceptor(String interceptorName, InterceptorDto interceptorDto) {
        Interceptor value = mapper.toDomain(interceptorDto);
        interceptorService.update(interceptorName, value);
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
