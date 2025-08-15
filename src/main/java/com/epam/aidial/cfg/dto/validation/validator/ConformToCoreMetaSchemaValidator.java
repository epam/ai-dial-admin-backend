package com.epam.aidial.cfg.dto.validation.validator;

import com.epam.aidial.core.config.CoreApplicationTypeSchema;
import jakarta.validation.ConstraintValidatorContext;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConformToCoreMetaSchemaValidator extends AbstractConformToCoreMetaSchemaValidator<CoreApplicationTypeSchema> {

    @SneakyThrows
    @Override
    public boolean isValid(CoreApplicationTypeSchema dto, ConstraintValidatorContext context) {
        return super.isValid(dto, dto.getId(), context);
    }
}
