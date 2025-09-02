package com.epam.aidial.cfg.service.hashing;

import com.epam.aidial.cfg.configuration.JsonMapperConfiguration;
import com.epam.aidial.cfg.domain.model.Model;
import com.epam.aidial.cfg.utils.ResourceUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class HashCalculatorTest {
    private final ObjectMapper objectMapper = JsonMapperConfiguration.createJsonMapper();
    private final HashCalculator calculator = new HashCalculator(objectMapper);

    @Test
    public void calculateHash() throws JsonProcessingException {
        var dtoJson = ResourceUtils.readResource("/domain/model/model.json");
        var model = objectMapper.readValue(dtoJson, Model.class);
        var actualHash = calculator.calculateHash(model);
        model.setUpdatedAt(null);
        model.setCreatedAt(null);
        var expectedHash = calculator.calculateHash(model);
        Assertions.assertEquals(expectedHash, actualHash);
    }
}