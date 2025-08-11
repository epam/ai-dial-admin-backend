package com.epam.aidial.cfg.dto.validation.validator;

import com.epam.aidial.cfg.dto.ApplicationTypeSchemaDto;
import jakarta.validation.ConstraintValidatorContext;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConformToCoreMetaSchemaDtoValidator extends AbstractConformToCoreMetaSchemaValidator<ApplicationTypeSchemaDto> {

    @SneakyThrows
    @Override
    public boolean isValid(ApplicationTypeSchemaDto dto, ConstraintValidatorContext context) {
        return super.isValid(dto, dto.getId(), context);
    }
}
