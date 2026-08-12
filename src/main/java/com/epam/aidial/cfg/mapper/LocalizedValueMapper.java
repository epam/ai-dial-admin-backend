package com.epam.aidial.cfg.mapper;

import com.epam.aidial.cfg.dto.LocalizedValueDto;
import com.epam.aidial.cfg.model.LocalizedValue;
import org.springframework.stereotype.Component;

@Component
public class LocalizedValueMapper {

    public LocalizedValueDto toDto(LocalizedValue domain) {
        if (domain == null) {
            return null;
        }
        return domain.isPlain() ? LocalizedValueDto.of(domain.getPlainValue()) : LocalizedValueDto.of(domain.getLocaleMap());
    }

    public LocalizedValue toDomain(LocalizedValueDto dto) {
        if (dto == null) {
            return null;
        }
        return dto.isPlain() ? LocalizedValue.of(dto.getPlainValue()) : LocalizedValue.of(dto.getLocaleMap());
    }
}
