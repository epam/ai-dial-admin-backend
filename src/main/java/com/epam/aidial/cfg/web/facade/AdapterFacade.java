package com.epam.aidial.cfg.web.facade;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.domain.model.Adapter;
import com.epam.aidial.cfg.domain.service.AdapterService;
import com.epam.aidial.cfg.dto.AdapterDto;
import com.epam.aidial.cfg.dto.DtoWithDomainHash;
import com.epam.aidial.cfg.web.facade.mapper.AdapterDtoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@LogExecution
public class AdapterFacade {

    private final AdapterService adapterService;
    private final AdapterDtoMapper mapper;

    public Collection<AdapterDto> getAllAdapters() {
        return adapterService.getAll()
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    public AdapterDto getAdapter(String adapterName) {
        Adapter adapter = adapterService.get(adapterName);
        return mapper.toDto(adapter);
    }

    public DtoWithDomainHash<AdapterDto> getAdapterWithHash(String adapterName) {
        var modelWithHash = adapterService.getAdapterWithHash(adapterName);
        AdapterDto dto = mapper.toDto(modelWithHash.model());
        return new DtoWithDomainHash<>(dto, modelWithHash.hash());
    }

    public void createAdapter(AdapterDto adapterDto) {
        Optional.of(adapterDto)
                .map(mapper::toDomain)
                .ifPresent(adapterService::create);
    }

    public String updateAdapter(String adapterName,
                                AdapterDto adapterDto,
                                String hash) {
        Adapter value = mapper.toDomain(adapterDto);
        return adapterService.update(adapterName, value, hash);
    }

    public void deleteAdapter(String adapterName, boolean removeModel) {
        adapterService.delete(adapterName, removeModel);
    }

    public AdapterDto getSnapshot(String adapterName, Integer revision) {
        Adapter adapter = adapterService.getSnapshot(adapterName, revision);
        return mapper.toDto(adapter);
    }

    public Collection<AdapterDto> getAllAtRevision(Integer revision) {
        return adapterService.getAllAtRevision(revision)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }
}
