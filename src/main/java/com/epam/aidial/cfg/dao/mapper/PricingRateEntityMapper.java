package com.epam.aidial.cfg.dao.mapper;

import com.epam.aidial.cfg.domain.model.PricingRate;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.mapstruct.Mapper;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring")
public class PricingRateEntityMapper {

    @Autowired
    protected ObjectMapper objectMapper;

    @SneakyThrows
    public PricingRate mapToPricingRate(String value) {
        if (value == null) {
            return null;
        }
        return objectMapper.readValue(value, PricingRate.class);
    }

    @SneakyThrows
    public String mapFromPricingRate(PricingRate value) {
        if (value == null) {
            return null;
        }
        return objectMapper.writeValueAsString(value);
    }
}
