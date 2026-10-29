package com.epam.aidial.cfg.domain.model.databind;

import com.epam.aidial.cfg.domain.model.UpstreamAuthConfig;
import com.epam.aidial.cfg.domain.model.UpstreamAuthType;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.exc.InvalidFormatException;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Accepts either a plain auth type string (the pre-existing shape, case-insensitive) or a
 * {@code {type, headerName}} object, producing a unified {@link UpstreamAuthConfig}. Mirrors Core's rules:
 * {@code headerName} is required, and must be a valid HTTP header name, for {@code CUSTOM_HEADER} and
 * is not allowed for any other type, so the plain string form can't express {@code CUSTOM_HEADER}.
 */
public class UpstreamAuthConfigDeserializer extends ValueDeserializer<UpstreamAuthConfig> {

    private static final Pattern HEADER_NAME_PATTERN = Pattern.compile("^[A-Za-z0-9!#$%&'*+.^_`|~-]+$");

    @Override
    public UpstreamAuthConfig deserialize(JsonParser p, DeserializationContext ctx) throws JacksonException {
        JsonToken token = p.currentToken();
        if (token == JsonToken.VALUE_STRING) {
            return validated(parseType(p, p.getString()), null, ctx);
        }

        if (token == JsonToken.START_OBJECT) {
            UpstreamAuthType type = null;
            String headerName = null;
            while (p.nextToken() != JsonToken.END_OBJECT) {
                String name = p.currentName();
                p.nextToken();
                switch (name) {
                    case "type" -> type = p.currentToken() == JsonToken.VALUE_NULL ? null : parseType(p, p.getString());
                    case "headerName" -> headerName = p.currentToken() == JsonToken.VALUE_NULL ? null : p.getString();
                    default -> p.skipChildren();
                }
            }
            return validated(type, headerName, ctx);
        }

        if (token == JsonToken.VALUE_NULL) {
            return null;
        }

        return ctx.reportInputMismatch(UpstreamAuthConfig.class,
                "Expected an auth type string or a {type, headerName} object, got %s", token);
    }

    private static UpstreamAuthType parseType(JsonParser p, String value) throws JacksonException {
        try {
            return UpstreamAuthType.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw InvalidFormatException.from(p, "Unknown upstream auth type", value, UpstreamAuthConfig.class);
        }
    }

    private static UpstreamAuthConfig validated(UpstreamAuthType type, String headerName, DeserializationContext ctx) {
        if (type == null) {
            return ctx.reportInputMismatch(UpstreamAuthConfig.class, "Authentication type is required");
        }
        if (type == UpstreamAuthType.CUSTOM_HEADER) {
            if (headerName == null || !HEADER_NAME_PATTERN.matcher(headerName).matches()) {
                return ctx.reportInputMismatch(UpstreamAuthConfig.class,
                        "%s requires a valid HTTP header name", UpstreamAuthType.CUSTOM_HEADER);
            }
        } else if (headerName != null) {
            return ctx.reportInputMismatch(UpstreamAuthConfig.class,
                    "headerName is only supported for %s", UpstreamAuthType.CUSTOM_HEADER);
        }
        return new UpstreamAuthConfig(type, headerName);
    }
}
