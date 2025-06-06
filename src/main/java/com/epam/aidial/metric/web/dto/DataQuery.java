package com.epam.aidial.metric.web.dto;


import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "$type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = JsonDataQuery.class, name = "json"),
        @JsonSubTypes.Type(value = SqlDataQuery.class, name = "sql"),
})
public interface DataQuery {

}
