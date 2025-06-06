package com.epam.aidial.cfg.domain.service;

import com.epam.aidial.cfg.dao.jpa.AssistantsPropertyJpaRepository;
import com.epam.aidial.cfg.dao.mapper.AssistantsPropertyEntityMapper;
import com.epam.aidial.cfg.dao.model.AssistantsPropertyEntity;
import com.epam.aidial.cfg.dao.model.FeaturesEntity;
import com.epam.aidial.cfg.domain.model.AssistantsProperty;
import com.epam.aidial.cfg.features.flag.annotation.FeatureFlagGate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class AssistantsPropertyService {

    private static final long ROW_ID = 1L;

    private final AssistantsPropertyJpaRepository assistantsPropertyJpaRepository;
    private final AssistantsPropertyEntityMapper mapper;

    @Transactional
    public AssistantsProperty getAssistantsProperty() {
        AssistantsPropertyEntity assistantsProperty = getAssistantsPropertyEntity();
        return mapper.toDomain(assistantsProperty);
    }

    @FeatureFlagGate(featureFlag = "assistantsSupported")
    @Transactional
    public void updateAssistantsProperty(AssistantsProperty assistantsProperty) {
        Optional.ofNullable(assistantsProperty)
                .map(this::toEntity)
                .map(assistantsPropertyJpaRepository::save)
                .orElseThrow(() -> new RuntimeException("unable to save assistants properties " + assistantsProperty));
    }

    private AssistantsPropertyEntity getAssistantsPropertyEntity() {
        return assistantsPropertyJpaRepository.findById(ROW_ID)
                .orElseGet(this::createEntityInstance);
    }

    private AssistantsPropertyEntity toEntity(AssistantsProperty assistantsProperty) {
        return mapper.toEntity(assistantsProperty, getAssistantsPropertyEntity());
    }

    private AssistantsPropertyEntity createEntityInstance() {
        AssistantsPropertyEntity entity = new AssistantsPropertyEntity();
        entity.setId(ROW_ID);
        entity.setFeatures(new FeaturesEntity());
        return entity;
    }
}
