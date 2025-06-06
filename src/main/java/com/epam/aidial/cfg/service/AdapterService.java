package com.epam.aidial.cfg.service;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.dto.AdapterDto;
import org.springframework.util.Assert;

import java.util.List;

@LogExecution
public class AdapterService {

    private final List<AdapterDto> adapters;

    public AdapterService(List<AdapterDto> adapters) {
        Assert.notNull(adapters, "adapters must be not null");
        this.adapters = adapters;
    }

    public List<AdapterDto> getAllAdapters() {
        return adapters;
    }
}
