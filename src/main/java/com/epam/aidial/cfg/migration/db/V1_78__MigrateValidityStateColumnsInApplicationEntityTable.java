package com.epam.aidial.cfg.migration.db;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.networknt.schema.ExecutionContext;
import com.networknt.schema.Format;
import com.networknt.schema.JsonMetaSchema;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.JsonType;
import com.networknt.schema.NonValidationKeyword;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.TypeFactory;
import com.networknt.schema.ValidationContext;
import com.networknt.schema.ValidationMessage;
import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
//CHECKSTYLE:OFF
public class V1_78__MigrateValidityStateColumnsInApplicationEntityTable extends BaseJavaMigration {

    //CHECKSTYLE:ON

    private static final String SELECT_APPLICATIONS_WITH_SCHEMA = """
            select app.deployment_name, app.application_properties,
            app_schema.schema_id, app_schema.schema, app_schema.defs, app_schema.properties, app_schema.required
            from application_entity app
            join application_type_schema_entity app_schema on app_schema.schema_id = app.application_type_schema_id""";

    private static final String UPDATE_APPLICATION_VALIDITY_STATE = """
            update application_entity set validity_state_message = ?, validity_state_is_valid = ?
            where deployment_name = ?""";

    private static final JsonMetaSchema.Builder METASCHEMA_BUILDER = JsonMetaSchema.builder("https://dial.epam.com/application_type_schemas/schema#", JsonMetaSchema.getV7())
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

    private static final JsonMetaSchema DIAL_META_SCHEMA = METASCHEMA_BUILDER
            .keyword(new NonValidationKeyword("dial:meta"))
            .keyword(new NonValidationKeyword("dial:file"))
            .keyword(new NonValidationKeyword("dial:resource"))
            .build();

    @Override
    public void migrate(Context context) throws Exception {
        Connection connection = context.getConnection();
        ObjectMapper objectMapper = new ObjectMapper();

        try (Statement selectStatement = connection.createStatement();
                ResultSet result = selectStatement.executeQuery(SELECT_APPLICATIONS_WITH_SCHEMA);
                PreparedStatement updateStatement = connection.prepareStatement(UPDATE_APPLICATION_VALIDITY_STATE)) {

            while (result.next()) {
                Application application = readApp(objectMapper, result);
                ApplicationTypeSchema applicationTypeSchema = readAppTypeSchema(objectMapper, result);

                Set<ValidationMessage> validationMessages = validateAppProperties(
                        objectMapper,
                        application.applicationProperties(),
                        applicationTypeSchema
                );

                if (!validationMessages.isEmpty()) {
                    String validityStateMessage = validationMessages.stream()
                            .map(ValidationMessage::getMessage)
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

    private Application readApp(ObjectMapper objectMapper, ResultSet result) throws SQLException, JsonProcessingException {
        return new Application(
                result.getString("deployment_name"),
                map(objectMapper, result.getString("application_properties"))
        );
    }

    private ApplicationTypeSchema readAppTypeSchema(ObjectMapper objectMapper, ResultSet result) throws SQLException, JsonProcessingException {
        return new ApplicationTypeSchema(
                result.getString("schema_id"),
                result.getString("schema"),
                map(objectMapper, result.getString("defs")),
                map(objectMapper, result.getString("properties")),
                readArray(result, "required")
        );
    }

    private Object readArray(ResultSet resultSet, String columnName) throws SQLException {
        var value = resultSet.getArray(columnName);
        return resultSet.wasNull() ? null : value.getArray();
    }

    private <T> Map<String, T> map(ObjectMapper objectMapper, String value) throws JsonProcessingException {
        if (value == null) {
            return Map.of();
        }
        return objectMapper.readValue(value, new TypeReference<>() {
        });
    }

    private Set<ValidationMessage> validateAppProperties(ObjectMapper objectMapper,
                                                         Map<String, Object> appProperties,
                                                         ApplicationTypeSchema applicationTypeSchema) throws JsonProcessingException, URISyntaxException {
        String schemaId = applicationTypeSchema.schemaId();

        Map<String, String> applicationTypeSchemas = Map.of(schemaId, objectMapper.writeValueAsString(applicationTypeSchema));

        JsonSchemaFactory schemaFactory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7, builder ->
                builder.schemaLoaders(loaders -> loaders.schemas(applicationTypeSchemas))
                        .metaSchema(DIAL_META_SCHEMA)
        );
        JsonNode applicationNode = objectMapper.valueToTree(appProperties);
        JsonSchema schema = schemaFactory.getSchema(new URI(schemaId));

        return schema.validate(applicationNode);
    }

    private record Application(String name, Map<String, Object> applicationProperties) {
    }

    private record ApplicationTypeSchema(@JsonProperty("$id")
                                         String schemaId,
                                         @JsonProperty("$schema")
                                         String schema,
                                         @JsonSerialize(using = JsonMapSerializer.class)
                                         Map<String, String> defs,
                                         @JsonSerialize(using = JsonMapSerializer.class)
                                         Map<String, String> properties,
                                         Object required) {
    }

    private static class JsonMapSerializer extends JsonSerializer<Map<String, String>> {

        @Override
        public void serialize(Map<String, String> map, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
            jsonGenerator.writeStartObject();

            for (Map.Entry<String, String> entry : map.entrySet()) {
                jsonGenerator.writeFieldName(entry.getKey());
                jsonGenerator.writeRawValue(entry.getValue());
            }

            jsonGenerator.writeEndObject();
        }
    }

    private static class DialFileFormat implements Format {

        private static final Pattern PATTERN = Pattern.compile("^files/([a-zA-Z0-9]+)/((?:(?:[a-zA-Z0-9_\\-.~]|%[a-zA-Z0-9]{2})+/?)+)$");

        @Override
        public boolean matches(ExecutionContext executionContext, ValidationContext validationContext, JsonNode value) {
            JsonType nodeType = TypeFactory.getValueNodeType(value, validationContext.getConfig());
            if (nodeType != JsonType.STRING) {
                return false;
            }
            String nodeValue = value.textValue();
            Matcher matcher = PATTERN.matcher(nodeValue);
            return matcher.matches();
        }

        @Override
        public String getName() {
            return "dial-file-encoded";
        }
    }

}
