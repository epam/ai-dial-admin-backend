package com.epam.aidial.cfg.dto.revision;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;

@Data
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type", visible = true)
@JsonSubTypes({
        @JsonSubTypes.Type(value = GetRevisionByTimestampQuery.class, name = "GET_BY_TIMESTAMP"),
        @JsonSubTypes.Type(value = GetRevisionByIdQuery.class, name = "GET_BY_ID")
})
public class BaseGetRevisionQuery {
    protected String type;
}

