package com.epam.aidial.core.config.validation;

import com.epam.aidial.core.metaschemas.CatalogMetaSchemaHolder;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.ValidationMessage;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

@Slf4j
public class CatalogSchemaValidator {

    public static Set<ValidationMessage> validate(Map<String, Object> catalogProperties, URI catalogSchemaId, CatalogSchemaValidationContext validationContext) {
        return validateApplicationProperties(catalogProperties, catalogSchemaId, validationContext);
    }

    private static Set<ValidationMessage> validateApplicationProperties(Map<String, Object> catalogProperties, URI schemaId, CatalogSchemaValidationContext validationContext) {
        if (schemaId == null) {
            return Set.of();
        }
        if (catalogProperties == null) {
            return Set.of();
        }
        JsonSchemaFactory schemaFactory = validationContext.getSchemaFactory();
        ObjectMapper mapper = validationContext.getMapper();
        JsonNode propertiesNode = mapper.valueToTree(catalogProperties);
        JsonSchema schema = schemaFactory.getSchema(schemaId);
        Set<ValidationMessage> validationResults = schema.validate(propertiesNode);
        validateDefaultLocales(schema.getSchemaNode(), propertiesNode, validationResults);
        return validationResults;
    }

    private static void validateDefaultLocales(JsonNode schemaNode, JsonNode propertiesNode, Set<ValidationMessage> validationResults) {
        String schemaDefaultLocale = schemaNode.path(CatalogMetaSchemaHolder.CATALOG_DEFAULT_LOCALE).asText("en");
        for (String localizedField : findLocalizedFieldNames(schemaNode)) {
            JsonNode fieldValue = propertiesNode.get(localizedField);
            if (fieldValue != null && fieldValue.isObject() && !fieldValue.has(schemaDefaultLocale)) {
                validationResults.add(ValidationMessage.builder().message("catalog_properties." + localizedField + " should contain the default locale \"" + schemaDefaultLocale + "\"").build());
            }
        }
    }

    private static Set<String> findLocalizedFieldNames(JsonNode schemaNode) {
        Set<String> result = new LinkedHashSet<>();
        JsonNode properties = schemaNode.path("properties");
        Iterator<Map.Entry<String, JsonNode>> fields = properties.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> field = fields.next();
            boolean localized = field.getValue().path(CatalogMetaSchemaHolder.DIAL_META).path(CatalogMetaSchemaHolder.META_LOCALIZED).asBoolean(false);
            if (localized) {
                result.add(field.getKey());
            }
        }
        return result;
    }
}
