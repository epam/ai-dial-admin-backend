package com.epam.aidial.metric.service;

import com.epam.aidial.cfg.exception.EntityNotFoundException;
import com.epam.aidial.cfg.features.IsMetricsEnabledCondition;
import com.epam.aidial.metric.model.FieldAvailability;
import com.epam.aidial.metric.model.configuration.DatasetDeclaration;
import com.epam.aidial.metric.model.DatasetInfo;
import com.epam.aidial.ql.Engine;
import com.epam.aidial.ql.LanguageConverter;
import com.epam.aidial.ql.dto.CompletableDto;
import com.epam.aidial.ql.model.Data;
import com.epam.aidial.ql.model.Table;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Conditional(IsMetricsEnabledCondition.class)
public class MetricService {

    private final Map<String, Engine> engines;
    private final Map<String, LanguageConverter> languageConverters;
    private final Map<String, DatasetDeclaration> declarations;

    public MetricService(List<Engine> engines, DatasetDeclaration datasetDeclaration) {
        this.engines = engines.stream()
                .collect(Collectors.toMap(Engine::getName, Function.identity()));
        this.languageConverters = engines.stream()
                .collect(Collectors.toMap(Engine::getName, LanguageConverter::new));
        this.declarations = Map.of(datasetDeclaration.getName(), datasetDeclaration);
    }

    public List<DatasetInfo> getDatasets() {
        return declarations.values().stream()
                .sorted(Comparator.comparing(DatasetDeclaration::getName))
                .map(d -> DatasetInfo.builder()
                        .name(d.getName())
                        .maxTimeRangeMs(toMillis(d.getMaxTimeRange()))
                        .build())
                .toList();
    }

    private static Long toMillis(Duration duration) {
        return duration != null ? duration.toMillis() : null;
    }

    public List<String> getTables(String datasetName) {
        return getEngine(datasetName)
                .getTables().keySet().stream()
                .sorted()
                .toList();
    }

    public Table getTableSchema(String datasetName, String tableName) {
        return getEngine(datasetName)
                .getTables().get(tableName);
    }

    // TODO: implement
    public FieldAvailability getFieldAvailability(String datasetName, String tableName, String columnName) {
        return null;
    }

    public Data getData(String datasetName, CompletableDto completableDto) {
        var engine = getEngine(datasetName);
        var completable = languageConverters.get(datasetName)
                .convert(completableDto, engine.getTables());

        return getEngine(datasetName).getData(completable);
    }

    private Engine getEngine(String datasetName) {
        var engine = engines.get(datasetName);
        if (engine == null) {
            throw new EntityNotFoundException("Dataset with name does not exists: %s".formatted(datasetName));
        }
        return engine;
    }

}
