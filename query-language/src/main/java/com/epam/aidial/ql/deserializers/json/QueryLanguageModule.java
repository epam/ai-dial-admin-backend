package com.epam.aidial.ql.deserializers.json;

import com.epam.aidial.ql.common.deserializer.CommonFilterDeserializer;
import com.epam.aidial.ql.dto.CompletableDto;
import com.epam.aidial.ql.dto.ExpressionDto;
import com.epam.aidial.ql.dto.FilterDto;
import com.epam.aidial.ql.dto.FromDto;
import com.epam.aidial.ql.dto.SortDto;
import com.epam.aidial.ql.dto.filters.AndDto;
import com.epam.aidial.ql.dto.filters.BinaryComparisonFilterDto;
import com.epam.aidial.ql.dto.filters.NotDto;
import com.epam.aidial.ql.dto.filters.OrDto;
import com.epam.aidial.ql.dto.filters.UnaryComparisonFilterDto;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.module.SimpleDeserializers;

public class QueryLanguageModule extends Module {
    @Override
    public String getModuleName() {
        return "QueryLanguageModule";
    }

    @Override
    public Version version() {
        return Version.unknownVersion();
    }

    @Override
    public void setupModule(SetupContext context) {
        final SimpleDeserializers deserializers = new SimpleDeserializers();
        deserializers.addDeserializer(ExpressionDto.class, new ExpressionDeserializer());
        deserializers.addDeserializer(FilterDto.class, new CommonFilterDeserializer<>(
            UnaryComparisonFilterDto::new,
            BinaryComparisonFilterDto::new,
            AndDto.class,
            OrDto.class,
            NotDto.class,
            ExpressionDto.class
        ));
        deserializers.addDeserializer(NotDto.class, new NotDeserializer());
        deserializers.addDeserializer(CompletableDto.class, new CompletableDeserializer());
        deserializers.addDeserializer(FromDto.class, new FromDeserializer());
        deserializers.addDeserializer(SortDto.class, new SortDeserializer());
        context.addDeserializers(deserializers);
    }
}
