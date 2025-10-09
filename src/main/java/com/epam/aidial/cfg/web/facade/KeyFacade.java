package com.epam.aidial.cfg.web.facade;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.domain.model.Interceptor;
import com.epam.aidial.cfg.domain.model.Key;
import com.epam.aidial.cfg.domain.service.KeyService;
import com.epam.aidial.cfg.dto.DtoWithDomainHash;
import com.epam.aidial.cfg.dto.InterceptorDto;
import com.epam.aidial.cfg.dto.KeyDto;
import com.epam.aidial.cfg.dto.ModelDto;
import com.epam.aidial.cfg.dto.route.RouteDto;
import com.epam.aidial.cfg.web.facade.mapper.KeyDtoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@LogExecution
public class KeyFacade {

    private final KeyService keyService;
    private final KeyDtoMapper mapper;

    public Collection<KeyDto> getAllKeys() {
        return keyService.getAllKeys()
                .stream()
                .map(mapper::toDtoWithoutKey)
                .collect(Collectors.toList());
    }

    public KeyDto getKey(String keyName) {
        Key key = keyService.getKey(keyName);
        return mapper.toDto(key);
    }

    public DtoWithDomainHash<KeyDto> getKeyWithHash(String keyName) {
        var modelWithHash = keyService.getKeyWithHash(keyName);
        var dto = mapper.toDto(modelWithHash.model());
        return new DtoWithDomainHash<>(dto, modelWithHash.hash());
    }

    public void createKey(KeyDto keyDto) {
        Optional.of(keyDto)
                .map(mapper::toDomain)
                .ifPresent(keyService::createKey);
    }

    public String updateKey(String keyName, KeyDto keyDto, String hash) {
        return keyService.updateKey(keyName, mapper.toDomain(keyDto), hash);
    }

    public void deleteKey(String keyName) {
        keyService.deleteKey(keyName);
    }

    public KeyDto getSnapshot(String keyName, Integer revision) {
        Key interceptor = keyService.getSnapshot(keyName, revision);
        return mapper.toDto(interceptor);
    }

    public Collection<KeyDto> getAllAtRevision(Integer revision) {
        return keyService.getAllAtRevision(revision)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }
}
