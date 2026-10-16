package com.epam.aidial.cfg.migration.db;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.networknt.schema.Error;
import com.networknt.schema.ExecutionContext;
import com.networknt.schema.Schema;
import com.networknt.schema.SchemaContext;
import com.networknt.schema.SchemaLocation;
import com.networknt.schema.SchemaRegistry;
import com.networknt.schema.SchemaRegistryConfig;
import com.networknt.schema.dialect.Dialect;
import com.networknt.schema.dialect.Dialects;
import com.networknt.schema.format.Format;
import com.networknt.schema.keyword.NonValidationKeyword;
import com.networknt.schema.path.PathType;
import com.networknt.schema.utils.JsonType;
import com.networknt.schema.utils.TypeFactory;
import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.annotation.JsonSerialize;
import tools.jackson.databind.json.JsonMapper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
//CHECKSTYLE:OFF
public class V1_79__MigrateValidityStateColumnsInApplicationEntityTableIncludingAppSchemaTypeField extends BaseJavaMigration {

    //CHECKSTYLE:ON

    private static final String SELECT_APPLICATIONS_WITH_SCHEMA = """
            select app.deployment_name, app.application_properties,
            app_schema.schema_id, app_schema.type, app_schema.defs, app_schema.properties, app_schema.required
            from application_entity app
            join application_type_schema_entity app_schema on app_schema.schema_id = app.application_type_schema_id""";

    private static final String UPDATE_APPLICATION_VALIDITY_STATE = """
            update application_entity set validity_state_message = ?, validity_state_is_valid = ?
            where deployment_name = ?""";

    private static final SchemaRegistryConfig SCHEMA_REGISTRY_CONFIG = SchemaRegistryConfig.builder().pathType(PathType.LEGACY).build();
    private static final Dialect.Builder DIALECT_BUILDER = Dialect.builder("https://dial.epam.com/application_type_schemas/schema#", Dialects.getDraft7())
            .keyword(new NonValidationKeyword("dial:applicationTypeEditorUrl"))
            .keyword(new NonValidationKeyword("dial:applicationTypeViewerUrl"))
            .keyword(new NonValidationKeyword("dial:applicationTypeDisplayName"))
            .keyword(new NonValidationKeyword("dial:applicationTypeCompletionEndpoint"))
            .keyword(new NonValidationKeyword("dial:applicationTypeConfigurationEndpoint"))
            .keyword(new NonValidationKeyword("dial:applicationTypeRateEndpoint"))
            .keyword(new NonValidationKeyword("dial:applicationTypeTokenizeEndpoint"))
            .keyword(new NonValidationKeyword("dial:applicationTypeTruncatePromptEndpoint"))
            .keyword(new NonValidationKeyword("dial:appendApplicationPropertiesHeader"))
            .keyword(new NonValidationKeyword("dial:applicationTypeIconUrl"))
            .keyword(new NonValidationKeyword("dial:applicationTypeRoutes"))
            .keyword(new NonValidationKeyword("dial:applicationTypePlaybackSupport"))
            .keyword(new NonValidationKeyword("dial:applicationTypeBucketCopy"))
            .keyword(new NonValidationKeyword("dial:applicationTypeInterceptors"))
            .keyword(new NonValidationKeyword("dial:propertyKind"))
            .keyword(new NonValidationKeyword("dial:propertyOrder"))
            .keyword(new NonValidationKeyword("$defs"))
            .format(new DialFileFormat());

    private static final Dialect DIAL_DIALECT = DIALECT_BUILDER
            .keyword(new NonValidationKeyword("dial:meta"))
            .keyword(new NonValidationKeyword("dial:file"))
            .keyword(new NonValidationKeyword("dial:resource"))
            .build();

    @Override
    public void migrate(Context context) throws Exception {
        Connection connection = context.getConnection();
        ObjectMapper objectMapper = JsonMapper.builder().build();

        try (Statement selectStatement = connection.createStatement();
                ResultSet result = selectStatement.executeQuery(SELECT_APPLICATIONS_WITH_SCHEMA);
                PreparedStatement updateStatement = connection.prepareStatement(UPDATE_APPLICATION_VALIDITY_STATE)) {

            while (result.next()) {
                Application application = readApp(objectMapper, result);
                ApplicationTypeSchema applicationTypeSchema = readAppTypeSchema(objectMapper, result);

                List<Error> validationMessages = validateAppProperties(
                        objectMapper,
                        application.applicationProperties(),
                        applicationTypeSchema
                );

                if (!validationMessages.isEmpty()) {
                    String validityStateMessage = validationMessages.stream()
                            .map(Error::toString)
                            .collect(Collectors.joining(", "));
                    updateStatement.setString(1, validityStateMessage);
                    updateStatement.setBoolean(2, false);
                } else {
                    updateStatement.setNull(1, java.sql.Types.VARCHAR);
                    updateStatement.setBoolean(2, true);
                }

                updateStatement.setString(3, application.name());
                updateStatement.addBatch();
            }

            updateStatement.executeBatch();
        }
    }

    private Application readApp(ObjectMapper objectMapper, ResultSet result) throws SQLException, JacksonException {
        return new Application(
                result.getString("deployment_name"),
                map(objectMapper, result.getString("application_properties"))
        );
    }

    private ApplicationTypeSchema readAppTypeSchema(ObjectMapper objectMapper, ResultSet result) throws SQLException, JacksonException {
        return new ApplicationTypeSchema(
                result.getString("schema_id"),
                readType(result),
                map(objectMapper, result.getString("defs")),
                map(objectMapper, result.getString("properties")),
                readArray(result, "required")
        );
    }

    private ApplicationTypeSchema.Type readType(ResultSet resultSet) throws SQLException {
        var value = resultSet.getString("type");
        return resultSet.wasNull() ? null : ApplicationTypeSchema.Type.valueOf(value);
    }

    private Object readArray(ResultSet resultSet, String columnName) throws SQLException {
        var value = resultSet.getArray(columnName);
        return resultSet.wasNull() ? null : value.getArray();
    }

    private <T> Map<String, T> map(ObjectMapper objectMapper, String value) throws JacksonException {
        if (value == null) {
            return Map.of();
        }
        return objectMapper.readValue(value, new TypeReference<>() {
        });
    }

    private List<Error> validateAppProperties(ObjectMapper objectMapper,
                                              Map<String, Object> appProperties,
                                              ApplicationTypeSchema applicationTypeSchema) throws JacksonException {
        String schemaId = applicationTypeSchema.schemaId();

        Map<String, String> applicationTypeSchemas = Map.of(schemaId, objectMapper.writeValueAsString(applicationTypeSchema));

        SchemaRegistry schemaRegistry = SchemaRegistry.withDialects(
                List.of(Dialects.getDraft7(), DIAL_DIALECT),
                builder -> builder.schemas(applicationTypeSchemas).schemaRegistryConfig(SCHEMA_REGISTRY_CONFIG)
        );
        JsonNode applicationNode = objectMapper.valueToTree(appProperties);
        Schema schema = schemaRegistry.getSchema(SchemaLocation.of(schemaId));

        return schema.validate(applicationNode);
    }

    private record Application(String name, Map<String, Object> applicationProperties) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private record ApplicationTypeSchema(@JsonProperty("$id")
                                         String schemaId,
                                         Type type,
                                         @JsonProperty("$defs")
                                         @JsonSerialize(using = JsonMapSerializer.class)
                                         Map<String, String> defs,
                                         @JsonSerialize(using = JsonMapSerializer.class)
                                         Map<String, String> properties,
                                         Object required) {

        private enum Type {
            OBJECT,
            BOOLEAN;

            @JsonValue
            public String toLower() {
                return name().toLowerCase();
            }
        }
    }

    private static class JsonMapSerializer extends ValueSerializer<Map<String, String>> {

        @Override
        public void serialize(Map<String, String> map, JsonGenerator jsonGenerator, SerializationContext serializerProvider) throws JacksonException {
            jsonGenerator.writeStartObject();

            for (Map.Entry<String, String> entry : map.entrySet()) {
                jsonGenerator.writeName(entry.getKey());
                jsonGenerator.writeRawValue(entry.getValue());
            }

            jsonGenerator.writeEndObject();
        }
    }

    private static class DialFileFormat implements Format {

        private static final Pattern PATTERN = Pattern.compile("^files/([a-zA-Z0-9]+)/((?:(?:[a-zA-Z0-9_\\-.~]|%[a-zA-Z0-9]{2})+/?)+)$");

        @Override
        public boolean matches(ExecutionContext executionContext, SchemaContext schemaContext, JsonNode value) {
            JsonType nodeType = TypeFactory.getValueNodeType(value, schemaContext.getSchemaRegistryConfig());
            if (nodeType != JsonType.STRING) {
                return false;
            }
            String nodeValue = value.asString();
            Matcher matcher = PATTERN.matcher(nodeValue);
            return matcher.matches();
        }

        @Override
        public String getName() {
            return "dial-file-encoded";
        }
    }

}
