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
        ApplicationTypeSchemaDto copy = new ApplicationTypeSchemaDto(dto);
        copy.setApplicationTypeRoutes(null); // meta schema has other format for routes
        return super.isValid(copy, copy.getId(), context);
    }
}
