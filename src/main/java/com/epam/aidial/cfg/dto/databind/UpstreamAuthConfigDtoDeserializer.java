package com.epam.aidial.cfg.dto.databind;

import com.epam.aidial.cfg.dto.UpstreamAuthConfigDto;
import com.epam.aidial.cfg.dto.UpstreamAuthTypeDto;
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
 * {@code {type, headerName}} object, producing a unified {@link UpstreamAuthConfigDto}. Mirrors Core's rules:
 * {@code headerName} is required, and must be a valid HTTP header name, for {@code CUSTOM_HEADER} and
 * is not allowed for any other type, so the plain string form can't express {@code CUSTOM_HEADER}.
 */
public class UpstreamAuthConfigDtoDeserializer extends ValueDeserializer<UpstreamAuthConfigDto> {

    private static final Pattern HEADER_NAME_PATTERN = Pattern.compile("^[A-Za-z0-9!#$%&'*+.^_`|~-]+$");

    @Override
    public UpstreamAuthConfigDto deserialize(JsonParser p, DeserializationContext ctx) throws JacksonException {
        JsonToken token = p.currentToken();
        if (token == JsonToken.VALUE_STRING) {
            return validated(parseType(p, p.getString()), null, ctx);
        }

        if (token == JsonToken.START_OBJECT) {
            UpstreamAuthTypeDto type = null;
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

        return ctx.reportInputMismatch(UpstreamAuthConfigDto.class,
                "Expected an auth type string or a {type, headerName} object, got %s", token);
    }

    private static UpstreamAuthTypeDto parseType(JsonParser p, String value) throws JacksonException {
        try {
            return UpstreamAuthTypeDto.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw InvalidFormatException.from(p, "Unknown upstream auth type", value, UpstreamAuthConfigDto.class);
        }
    }

    private static UpstreamAuthConfigDto validated(UpstreamAuthTypeDto type, String headerName, DeserializationContext ctx) {
        if (type == null) {
            return ctx.reportInputMismatch(UpstreamAuthConfigDto.class, "Authentication type is required");
        }
        if (type == UpstreamAuthTypeDto.CUSTOM_HEADER) {
            if (headerName == null || !HEADER_NAME_PATTERN.matcher(headerName).matches()) {
                return ctx.reportInputMismatch(UpstreamAuthConfigDto.class,
                        "%s requires a valid HTTP header name", UpstreamAuthTypeDto.CUSTOM_HEADER);
            }
        } else if (headerName != null) {
            return ctx.reportInputMismatch(UpstreamAuthConfigDto.class,
                    "headerName is only supported for %s", UpstreamAuthTypeDto.CUSTOM_HEADER);
        }
        return new UpstreamAuthConfigDto(type, headerName);
    }
}
