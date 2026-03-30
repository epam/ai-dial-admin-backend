package com.epam.aidial.metric.web.controller;

import com.epam.aidial.cfg.exception.EntityNotFoundException;
import com.epam.aidial.cfg.features.IsMetricsEnabledCondition;
import com.epam.aidial.expressions.AggregationFunctionCall;
import com.epam.aidial.expressions.Column;
import com.epam.aidial.expressions.Expression;
import com.epam.aidial.expressions.GroupFunctionCall;
import com.epam.aidial.expressions.enums.Type;
import com.epam.aidial.metric.model.FieldAvailability;
import com.epam.aidial.metric.model.configuration.ColumnType;
import com.epam.aidial.metric.service.MetricService;
import com.epam.aidial.metric.web.dto.ColumnDeclarationDto;
import com.epam.aidial.metric.web.dto.DataDto;
import com.epam.aidial.metric.web.dto.DataQuery;
import com.epam.aidial.metric.web.dto.DatasetInfoDto;
import com.epam.aidial.metric.web.dto.JsonDataQuery;
import com.epam.aidial.metric.web.dto.SqlDataQuery;
import com.epam.aidial.metric.web.dto.TableSchemaDto;
import com.epam.aidial.ql.deserializers.sql.QueryParserUtil;
import com.epam.aidial.ql.dto.CompletableDto;
import com.epam.aidial.ql.model.Data;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.context.annotation.Conditional;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Slf4j
@RestController
@RequestMapping("/api/v1/metrics/")
@Validated
@RequiredArgsConstructor
@Conditional(IsMetricsEnabledCondition.class)
public class MetricController {

    private final MetricService metricService;

    @GetMapping(path = "/datasets",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public List<DatasetInfoDto> getDatasets() {
        return metricService.getDatasets();
    }

    @GetMapping(path = "/datasets/{dataset}/tables",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public List<String> getTables(@PathVariable("dataset") String datasetName) {
        return metricService.getTables(datasetName);
    }

    @GetMapping(path = "/datasets/{dataset}/tables/{table}/schema",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public TableSchemaDto getTableSchema(@PathVariable("dataset") String datasetName,
                                         @PathVariable("table") String tableName) {
        var table = metricService.getTableSchema(datasetName, tableName);
        if (table == null) {
            throw new EntityNotFoundException("Dataset with name: %s does not contain table with name: %s"
                    .formatted(datasetName, tableName));
        }

        // TODO: create mapper
        var columns = table.getColumns().values().stream()
                .map(column -> ColumnDeclarationDto.builder()
                        .name(column.getName())
                        .type(toType(column.getType()))
                        .build())
                .sorted(Comparator.comparing(ColumnDeclarationDto::getName))
                .toList();
        return TableSchemaDto.builder()
                .columns(columns)
                .build();
    }

    private ColumnType toType(Type type) {
        return switch (type) {
            case STRING -> ColumnType.STRING;
            case TIMESTAMP -> ColumnType.TIMESTAMP;
            default -> throw new IllegalArgumentException("Unsupported column type: " + type);
        };
    }

    // todo: return DTO not model
    @GetMapping(path = "/datasets/{dataset}/tables/{table}/schema/column/{columnName}/availability",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public FieldAvailability getFieldAvailability(@PathVariable("dataset") String datasetName,
                                                  @PathVariable("table") String tableName,
                                                  @PathVariable("columnName") String columnName) {
        return metricService.getFieldAvailability(datasetName, tableName, columnName);
    }

    @PostMapping(path = "/datasets/{dataset}/data",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = "text/csv")
    public ResponseEntity<byte[]> getCsvDataBy(@PathVariable("dataset") String datasetName,
                                               @RequestBody @Valid DataQuery query) throws IOException {
        var completableDto = getCompletableDto(query);
        var data = metricService.getData(datasetName, completableDto);
        var body = toByteArray(data);
        return ResponseEntity.ok(body);
    }

    @PostMapping(path = "/datasets/{dataset}/data",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DataDto> getJsonData(@PathVariable("dataset") String datasetName,
                                               @RequestBody @Valid DataQuery query) {
        var completableDto = getCompletableDto(query);
        var data = metricService.getData(datasetName, completableDto);
        var body = toDataDto(data);
        return ResponseEntity.ok(body);
    }

    @PostMapping(path = "/datasets/{dataset}/data",
            consumes = MediaType.TEXT_PLAIN_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DataDto> getJsonData(@PathVariable("dataset") String datasetName,
                                               @RequestBody @Valid String query) {
        return getJsonData(datasetName, new SqlDataQuery(query));
    }

    private CompletableDto getCompletableDto(DataQuery query) {
        CompletableDto completableDto;
        if (query instanceof SqlDataQuery sqlDataQuery) {
            completableDto = QueryParserUtil.parse(sqlDataQuery.getQuery());
        } else if (query instanceof JsonDataQuery jsonDataQuery) {
            completableDto = jsonDataQuery.getQuery();
        } else {
            throw new IllegalArgumentException("Unsupported query type: " + query.getClass());
        }
        return completableDto;
    }

    private byte[] toByteArray(Data data) throws IOException {
        var stringWriter = new StringWriter();
        try (var csvPrinter = new CSVPrinter(stringWriter, CSVFormat.DEFAULT.builder()
                .setHeader(getHeaders(data.getExpressions()).toArray(new String[0])).get())) {
            for (var row : data.getData()) {
                for (var value : row) {
                    csvPrinter.print(value);
                }
                csvPrinter.println();
            }
        }
        return stringWriter.toString().getBytes();
    }

    private DataDto toDataDto(Data data) {
        var headers = getHeaders(data.getExpressions());
        var stringifiedData = data.getData().stream()
                .map(list -> list.stream().map(v -> v == null ? null : Objects.toString(v)).toList())
                .toList();
        return DataDto.builder()
                .headers(headers)
                .data(stringifiedData)
                .build();
    }

    private List<String> getHeaders(List<Expression> expressions) {
        return expressions.stream().map(this::getHeader).toList();
    }

    private String getHeader(Expression expression) {
        if (expression instanceof Column column) {
            return column.getName();
        } else if (expression instanceof AggregationFunctionCall aggregationFunctionCall) {
            return aggregationFunctionCall.getFunction().getName();
        } else if (expression instanceof GroupFunctionCall groupFunctionCall) {
            return groupFunctionCall.getFunction().getName();
        } else {
            log.debug("Unsupported expression type encountered during conversion to header name. Expression type: {}",
                    expression.getClass().getName());
            return expression.toString();
        }
    }

}
