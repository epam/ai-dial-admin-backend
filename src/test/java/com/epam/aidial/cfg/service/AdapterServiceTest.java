package com.epam.aidial.cfg.service;

import com.epam.aidial.cfg.dto.AdapterDto;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

class AdapterServiceTest {

    @Test
    void getAllAdapters() {
        List<AdapterDto> adapters = List.of(new AdapterDto());
        AdapterService adapterService = new AdapterService(adapters);
        List<AdapterDto> actual = adapterService.getAllAdapters();
        Assertions.assertEquals(adapters, actual);
    }
}