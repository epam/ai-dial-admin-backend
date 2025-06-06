package com.epam.aidial.metric.component;

import com.epam.aidial.metric.model.configuration.DatasetDeclaration;
import com.epam.aidial.metric.util.CollectorsUtils;
import com.epam.aidial.ql.Engine;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

@Component
@Scope(SCOPE_PROTOTYPE)
@RequiredArgsConstructor
public class EngineFactoryManager {

    private final List<EngineFactory> engineFactories;

    public Engine build(DatasetDeclaration dataset) {
        var engineFactory = engineFactories.stream()
                .filter(factory -> factory.supports(dataset))
                .collect(CollectorsUtils.toSingleton(()
                        -> new IllegalStateException("Multiple factories found for dataset %s".formatted(dataset))))
                .orElseThrow(()
                        -> new IllegalStateException("No factory found for dataset %s".formatted(dataset)));
        return engineFactory.createEngine(dataset);
    }

}
