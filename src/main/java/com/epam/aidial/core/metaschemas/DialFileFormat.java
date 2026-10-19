package com.epam.aidial.core.metaschemas;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.networknt.schema.ExecutionContext;
import com.networknt.schema.SchemaContext;
import com.networknt.schema.format.Format;
import com.networknt.schema.utils.JsonType;
import com.networknt.schema.utils.TypeFactory;
import tools.jackson.databind.JsonNode;

public class DialFileFormat implements Format {

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
