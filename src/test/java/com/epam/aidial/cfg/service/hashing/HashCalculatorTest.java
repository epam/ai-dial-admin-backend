package com.epam.aidial.cfg.service.hashing;

import com.epam.aidial.cfg.configuration.JsonMapperConfiguration;
import com.epam.aidial.cfg.domain.model.Model;
import com.epam.aidial.cfg.utils.ResourceUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

class HashCalculatorTest {

    private static final String EXPECTED_HASH_MODEL_JSON = "ZkDP6eTI3ypYUIsahKthDeb-ZgoJJ1U0cLgyJMGy37o";
    private final JsonMapper objectMapper = JsonMapperConfiguration.createJsonMapper();
    private final HashCalculator calculator = new HashCalculator(objectMapper);

    @Test
    public void calculateHash_matchesExpectedHash() throws JacksonException {
        var dtoJson = ResourceUtils.readResource("/domain/model/model.json");
        var model = objectMapper.readValue(dtoJson, Model.class);
        var actualHash = calculator.calculateHash(model);
        Assertions.assertEquals(EXPECTED_HASH_MODEL_JSON, actualHash);
    }

    @Test
    public void calculateHash_ignoresAuditFields() throws JacksonException {
        var dtoJson = ResourceUtils.readResource("/domain/model/model.json");
        var model = objectMapper.readValue(dtoJson, Model.class);
        var actualHash = calculator.calculateHash(model);
        model.setUpdatedAt(null);
        model.setCreatedAt(null);
        var expectedHash = calculator.calculateHash(model);
        Assertions.assertEquals(expectedHash, actualHash);
    }
}