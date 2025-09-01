package com.epam.aidial.cfg.service.hashing;

import com.epam.aidial.cfg.configuration.JsonMapperConfiguration;
import com.epam.aidial.cfg.domain.model.Model;
import com.epam.aidial.cfg.domain.utils.ModelEndpointUtils;
import com.epam.aidial.cfg.utils.ResourceUtils;
import com.epam.aidial.cfg.web.facade.mapper.FeaturesMapperImpl;
import com.epam.aidial.cfg.web.facade.mapper.InstantMapperImpl;
import com.epam.aidial.cfg.web.facade.mapper.LimitDtoMapperImpl;
import com.epam.aidial.cfg.web.facade.mapper.ModelDtoMapperImpl;
import com.epam.aidial.cfg.web.facade.mapper.ModelEndpointDtoMapperImpl;
import com.epam.aidial.cfg.web.facade.mapper.RoleBasedDtoMapperImpl;
import com.epam.aidial.cfg.web.facade.mapper.ShareResourceLimitDtoMapperImpl;
import com.epam.aidial.cfg.web.facade.mapper.UpstreamDtoMapperImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {JsonMapperConfiguration.class, HashCalculator.class,
    ModelDtoMapperImpl.class, ModelEndpointUtils.class,
    LimitDtoMapperImpl.class, UpstreamDtoMapperImpl.class, RoleBasedDtoMapperImpl.class,
    ModelEndpointDtoMapperImpl.class, InstantMapperImpl.class, FeaturesMapperImpl.class,
    ShareResourceLimitDtoMapperImpl.class})
class HashCalculatorTest {
    private final ObjectMapper objectMapper = JsonMapperConfiguration.createJsonMapper();
    @Autowired
    HashCalculator calculator;

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