package com.epam.aidial.cfg.web.facade;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.domain.model.AssistantsProperty;
import com.epam.aidial.cfg.domain.service.AssistantsPropertyService;
import com.epam.aidial.cfg.dto.AssistantsPropertyDto;
import com.epam.aidial.cfg.web.facade.mapper.AssistantsPropertyDtoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
@LogExecution
public class AssistantsPropertyFacade {

    private final AssistantsPropertyService assistantsPropertyService;
    private final AssistantsPropertyDtoMapper mapper;

    public AssistantsPropertyDto getAssistantsProperty() {
        AssistantsProperty domain = assistantsPropertyService.getAssistantsProperty();
        return mapper.toDto(domain);
    }

    public void updateAssistantsProperty(AssistantsPropertyDto assistantsProperty) {
        AssistantsProperty domain = mapper.toDomain(assistantsProperty);
        assistantsPropertyService.updateAssistantsProperty(domain);
    }
}
